package com.wifi.server.receiver;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wifi.server.R;
import com.wifi.server.base.BaseFragment;
import com.wifi.server.dialogs.AnimationView;
import com.wifi.server.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;


public class ReceiverFragment extends BaseFragment implements View.OnClickListener, TextWatcher {

    private Button btnSend;
    private Button btnGallery;
    private Button btnPicture;
    private EditText senderTxt;
    private ImageView image;
    private int GALLERY_REQUEST = 1;

    private View connectView, hotspotView;
    private AnimationView connectGif, hotspotGif;
    private TextView connectTxtView, hotspotTxtView;

    public static ReceiverFragment newInstance() {
        ReceiverFragment fragment = new ReceiverFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_receiver, container, false);

        connectView = rootView.findViewById(R.id.scan_progress);
        connectGif = rootView.findViewById(R.id.gif1);
        connectTxtView = rootView.findViewById(R.id.progress_txt);

        hotspotView = rootView.findViewById(R.id.scan_hotspot);
        hotspotGif = rootView.findViewById(R.id.gif2);
        hotspotTxtView = rootView.findViewById(R.id.progress_hotspot_txt);

        btnSend = rootView.findViewById(R.id.btn_send);
        btnGallery = rootView.findViewById(R.id.btn_gallery);
        btnPicture = rootView.findViewById(R.id.btn_picture);
        image = rootView.findViewById(R.id.id_image);
        btnSend.setEnabled(false);
        btnSend.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnPicture.setOnClickListener(this);

        senderTxt = rootView.findViewById(R.id.sender_txt);
        senderTxt.addTextChangedListener(this);

        return rootView;
    }


    public void onServerConnectStarted() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectView.setVisibility(View.VISIBLE);
                connectGif.setMovieResource(R.drawable.loader_2);
            }
        });
    }

    public void onServerConnectSuccess() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectGif.setVisibility(View.INVISIBLE);
                connectTxtView.setText(activity.getString(R.string.sender_connected));
                onDataTransferStarted();
            }
        });
    }

    public void onServerConnectFailed() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showMessage(activity, activity.getString(R.string.receiver_error_in_connecting));
                connectView.setVisibility(View.VISIBLE);
                connectTxtView.setText(getString(R.string.receiver_init));
            }
        });
    }

    public void onDataTransferStarted() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hotspotView.setVisibility(View.VISIBLE);
                hotspotGif.setMovieResource(R.drawable.loader_2);
                hotspotTxtView.setText(activity.getString(R.string.receiver_confirmation));
            }
        });
    }


    public String getMessage() {
        return senderTxt.getText().toString();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(senderTxt.getText().toString().length() > 0) {
            btnSend.setEnabled(true);
            btnSend.setBackgroundColor(ContextCompat.getColor(activity, R.color.btn_green_bg));

        } else btnSend.setEnabled(false);
    }

    @Override
    public void afterTextChanged(Editable editable) { }
    @Override
    public void onClick(View view) {
        if(view == btnSend) {
            //senderTxt.setEnabled(false);
            //btnSend.setEnabled(false);
            ((ReceiverActivity) getActivity()).startDataTransfer(getMessage());
        } else if(view == btnGallery) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
            //((ReceiverActivity) getActivity()).startDataTransfer(getMessage());
        } else if (view == btnPicture) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String imageString = Base64.getEncoder().encodeToString(imageBytes);
                ((ReceiverActivity) getActivity()).startDataTransfer(imageString);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case 1:
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                        image.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.i("TAG", "Some exception " + e);
                    }
                    break;
            }
    }
}