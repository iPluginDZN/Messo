package com.pplugin.messo_se.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pplugin.messo_se.R;
import com.pplugin.messo_se.ui.LoginActivity;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_profile, container, false);
        Button logoutButton = v.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login", getContext().MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();
                // Go to login activity
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        return v;
    }
}
