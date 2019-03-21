/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfmeshprovisioner.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.utils.AddressType;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.PublicationSettings;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.AddressTypeAdapterSpinner;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.GroupAdapterSpinner;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;

import static no.nordicsemi.android.meshprovisioner.utils.AddressType.GROUP_ADDRESS;
import static no.nordicsemi.android.meshprovisioner.utils.AddressType.UNASSIGNED_ADDRESS;
import static no.nordicsemi.android.meshprovisioner.utils.AddressType.UNICAST_ADDRESS;
import static no.nordicsemi.android.meshprovisioner.utils.AddressType.VIRTUAL_ADDRESS;

public class DialogFragmentPublishAddress extends DialogFragment {

    private static final String PUBLICATION_SETTINGS = "PUBLICATION_SETTINGS";
    private static final String GROUPS = "GROUPS";
    private static final String UUID_KEY = "UUID";
    private int mPublishAddress = 0;
    private ArrayList<Group> mGroups = new ArrayList<>();
    private PublicationSettings mPublicationSettings;
    private static final AddressType[] addressTypes = {UNASSIGNED_ADDRESS, UNICAST_ADDRESS, GROUP_ADDRESS, VIRTUAL_ADDRESS};

    //UI Bindings
    @BindView(R.id.address_types)
    Spinner addressTypesSpinnerView;
    @BindView(R.id.text_input_layout)
    TextInputLayout unicastAddressInputLayout;
    @BindView(R.id.text_input)
    TextInputEditText unicastAddressInput;
    @BindView(R.id.uuid_label)
    TextView labelUuidView;
    @BindView(R.id.group_container)
    View groupContainer;
    @BindView(R.id.radio_select_group)
    RadioButton selectGroup;
    @BindView(R.id.radio_create_group)
    RadioButton createGroup;
    @BindView(R.id.groups)
    Spinner groups;
    @BindView(R.id.group_name_layout)
    TextInputLayout groupNameInputLayout;
    @BindView(R.id.name_input)
    TextInputEditText groupNameInput;
    @BindView(R.id.group_address_layout)
    TextInputLayout addressInputLayout;
    @BindView(R.id.address_input)
    TextInputEditText addressInput;
    @BindView(R.id.no_groups_configured)
    TextView noGroups;
    private Button mGenerateLabelUUID;

    private AddressTypeAdapterSpinner mAdapterSpinner;

    public interface DialogFragmentPublishAddressListener {

        void setPublishAddress(@NonNull final AddressType addressType, final int publishAddress);

        void setPublishAddress(@NonNull final AddressType addressType, @NonNull final String name, final int address);

        void setPublishAddress(@NonNull final AddressType addressType, @NonNull final Group group);

        void setPublishAddress(@NonNull final AddressType addressType, @NonNull final UUID labelUuid);

    }

    public static DialogFragmentPublishAddress newInstance(final PublicationSettings publicationSettings, final ArrayList<Group> groups) {
        DialogFragmentPublishAddress fragmentPublishAddress = new DialogFragmentPublishAddress();
        final Bundle args = new Bundle();
        args.putParcelable(PUBLICATION_SETTINGS, publicationSettings);
        args.putParcelableArrayList(GROUPS, groups);
        fragmentPublishAddress.setArguments(args);
        return fragmentPublishAddress;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPublicationSettings = getArguments().getParcelable(PUBLICATION_SETTINGS);
            mGroups = getArguments().getParcelableArrayList(GROUPS);
            //mPublishAddress = getArguments().getInt(PUBLICATION_SETTINGS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        final View rootView = LayoutInflater.from(getContext()).
                inflate(R.layout.dialog_fragment_publish_address_input, null);

        //Bind ui
        ButterKnife.bind(this, rootView);
        if(savedInstanceState != null) {
            labelUuidView.setText(savedInstanceState.getString(UUID_KEY));
        }

        setAddressType();

        addressTypesSpinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                updateAddress(mAdapterSpinner.getItem(position));
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });

        selectGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            groupNameInputLayout.setEnabled(!isChecked);
            groupNameInputLayout.setError(null);
            addressInputLayout.setEnabled(!isChecked);
            addressInputLayout.setError(null);
            groups.setEnabled(isChecked);
            createGroup.setChecked(!isChecked);
        });

        createGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            groupNameInputLayout.setEnabled(isChecked);
            groupNameInputLayout.setError(null);
            addressInputLayout.setEnabled(isChecked);
            addressInputLayout.setError(null);
            groups.setEnabled(!isChecked);
            selectGroup.setChecked(!isChecked);
        });

        final GroupAdapterSpinner adapter = new GroupAdapterSpinner(requireContext(), mGroups);
        groups.setAdapter(adapter);

        if (mGroups.isEmpty()) {
            selectGroup.setEnabled(false);
            groups.setEnabled(false);
            createGroup.setChecked(true);
        } else {
            selectGroup.setChecked(true);
            createGroup.setChecked(false);
        }

        final KeyListener hexKeyListener = new HexKeyListener();
        addressInput.setKeyListener(hexKeyListener);
        addressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    addressInputLayout.setError(getString(R.string.error_empty_group_address));
                } else {
                    addressInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        unicastAddressInputLayout.setHint(getString((R.string.hint_publish_address)));
        unicastAddressInput.setKeyListener(hexKeyListener);
        unicastAddressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    unicastAddressInputLayout.setError(getString(R.string.error_empty_publish_address));
                } else {
                    unicastAddressInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext()).
                setIcon(R.drawable.ic_lan_black_alpha_24dp).
                setTitle(R.string.title_publish_address).
                setView(rootView).
                setPositiveButton(R.string.ok, null).
                setNegativeButton(R.string.cancel, null).
                setNeutralButton(R.string.generate_uuid, null);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> setPublishAddress());

        mGenerateLabelUUID = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        mGenerateLabelUUID.setOnClickListener(v -> labelUuidView.setText(MeshAddress.generateRandomLabelUUID().toString().toUpperCase()));

        return alertDialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(UUID_KEY, labelUuidView.getText().toString());
    }

    private void setPublishAddress() {
        final String input = unicastAddressInput.getEditableText().toString();
        final int address;
        final AddressType type = (AddressType) addressTypesSpinnerView.getSelectedItem();
        switch (type) {
            default:
            case UNASSIGNED_ADDRESS:
                if (validateInput(input)) {
                    address = Integer.parseInt(input, 16);
                    ((DialogFragmentPublishAddressListener) requireActivity()).
                            setPublishAddress(type, address);
                    dismiss();
                }
                break;
            case UNICAST_ADDRESS:
                if (validateInput(input)) {
                    address = Integer.parseInt(input, 16);
                    ((DialogFragmentPublishAddressListener) requireActivity()).
                            setPublishAddress(type, address);
                    dismiss();
                }
                break;
            case GROUP_ADDRESS:
                if (createGroup.isChecked()) {
                    final String name = groupNameInput.getEditableText().toString();
                    final String add = addressInput.getEditableText().toString();
                    if (validateInput(name, add)) {
                        ((DialogFragmentPublishAddressListener) requireActivity()).
                                setPublishAddress(type, name, Integer.valueOf(add, 16));
                        dismiss();
                    }
                } else {
                    final Group group = (Group) groups.getSelectedItem();
                    ((DialogFragmentPublishAddressListener) requireActivity()).setPublishAddress(type, group);
                    dismiss();
                }
                break;
            case VIRTUAL_ADDRESS:
                    final UUID uuid = UUID.fromString(labelUuidView.getText().toString());
                    ((DialogFragmentPublishAddressListener) requireActivity()).
                            setPublishAddress(type, uuid);
                    dismiss();
                break;
        }

    }

    private void setAddressType() {
        int address = 0;
        if (mPublicationSettings != null) {
            address = mPublicationSettings.getPublishAddress();
        }

        mAdapterSpinner = new AddressTypeAdapterSpinner(requireContext(), addressTypes);
        addressTypesSpinnerView.setAdapter(mAdapterSpinner);
        final AddressType type = MeshAddress.getAddressType(address);
        if (type != null) {
            switch (type) {
                default:
                case UNASSIGNED_ADDRESS:
                    addressTypesSpinnerView.setSelection(0);
                    break;
                case UNICAST_ADDRESS:
                    addressTypesSpinnerView.setSelection(1);
                    break;
                case GROUP_ADDRESS:
                    addressTypesSpinnerView.setSelection(2);
                    break;
                case VIRTUAL_ADDRESS:
                    addressTypesSpinnerView.setSelection(3);
                    break;
            }
        }
    }

    private void updateAddress(final AddressType addressType) {
        int address = 0;
        if (mPublicationSettings != null) {
            address = mPublicationSettings.getPublishAddress();
        }
        final String publishAddress;
        switch (addressType) {
            default:
            case UNASSIGNED_ADDRESS:
                publishAddress = MeshAddress.formatAddress(MeshAddress.UNASSIGNED_ADDRESS, false);
                unicastAddressInput.setText(publishAddress);
                unicastAddressInputLayout.setVisibility(View.VISIBLE);
                groupContainer.setVisibility(View.GONE);
                labelUuidView.setVisibility(View.GONE);
                mGenerateLabelUUID.setVisibility(View.GONE);
                break;
            case UNICAST_ADDRESS:
                publishAddress = MeshAddress.formatAddress(address, false);
                unicastAddressInput.setText(publishAddress);
                unicastAddressInputLayout.setVisibility(View.VISIBLE);
                groupContainer.setVisibility(View.GONE);
                labelUuidView.setVisibility(View.GONE);
                mGenerateLabelUUID.setVisibility(View.GONE);
                break;
            case GROUP_ADDRESS:
                final int index = getGroupIndex(address);
                groups.setSelection(index);
                groupContainer.setVisibility(View.VISIBLE);
                unicastAddressInputLayout.setVisibility(View.GONE);
                labelUuidView.setVisibility(View.GONE);
                mGenerateLabelUUID.setVisibility(View.GONE);
                break;
            case VIRTUAL_ADDRESS:
                if(mPublicationSettings != null && mPublicationSettings.getLabelUUID() != null){
                    labelUuidView.setText(mPublicationSettings.getLabelUUID().toString().toUpperCase(Locale.US));
                }
                labelUuidView.setVisibility(View.VISIBLE);
                mGenerateLabelUUID.setVisibility(View.VISIBLE);
                unicastAddressInputLayout.setVisibility(View.GONE);
                groupContainer.setVisibility(View.GONE);
                break;
        }
    }

    private int getGroupIndex(final int address) {
        for (int i = 0; i < mGroups.size(); i++) {
            if (address == mGroups.get(i).getGroupAddress()) {
                return i;
            }
        }
        return 0;
    }

    private boolean validateInput(@NonNull final String input) {

        try {
            if (input.length() % 4 != 0 || !input.matches(Utils.HEX_PATTERN)) {
                unicastAddressInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }
            final AddressType type = (AddressType) addressTypesSpinnerView.getSelectedItem();

            final int address = Integer.parseInt(input, 16);
            switch (type) {
                default:
                case UNASSIGNED_ADDRESS:
                    if (!MeshAddress.isValidUnassignedAddress(address)) {
                        unicastAddressInputLayout.setError(getString(R.string.invalid_address_value));
                        return false;
                    }
                    return true;
                case UNICAST_ADDRESS:
                    if (!MeshAddress.isValidUnicastAddress(address)) {
                        unicastAddressInputLayout.setError(getString(R.string.invalid_unicast_address));
                        return false;
                    }
                    return true;
                case GROUP_ADDRESS:
                    if (!MeshAddress.isValidGroupAddress(address)) {
                        addressInputLayout.setError(getString(R.string.invalid_group_address));
                        return false;
                    }
                    for (Group group : mGroups) {
                        if (address == group.getGroupAddress()) {
                            addressInputLayout.setError(getString(R.string.error_group_address_in_used));
                            return false;
                        }
                    }
                    return true;
                case VIRTUAL_ADDRESS:
                    //do nothing since the library generates it
                    return true;
            }

        } catch (IllegalArgumentException ex) {
            unicastAddressInputLayout.setError(ex.getMessage());
            return false;
        }
    }

    private boolean validateInput(@NonNull final String name, @NonNull final String address) {
        try {
            if (TextUtils.isEmpty(name)) {
                groupNameInputLayout.setError(getString(R.string.error_empty_group_name));
                return false;
            }
            if (address.length() % 4 != 0 || !address.matches(Utils.HEX_PATTERN)) {
                addressInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            final int groupAddress = Integer.valueOf(address, 16);
            if (!MeshAddress.isValidGroupAddress(groupAddress)) {
                addressInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            for (Group group : mGroups) {
                if (groupAddress == group.getGroupAddress()) {
                    addressInputLayout.setError(getString(R.string.error_group_address_in_used));
                    return false;
                }
            }
        } catch (IllegalArgumentException ex) {
            addressInputLayout.setError(ex.getMessage());
            return false;
        }

        return true;
    }
}
