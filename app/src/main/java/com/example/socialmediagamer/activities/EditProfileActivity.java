package com.example.socialmediagamer.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.models.User;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.ImageProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.example.socialmediagamer.utils.FileUtil;
import com.example.socialmediagamer.utils.ViewedMessageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.socialmediagamer.utils.Validations.validateFieldsAsYouType;

public class EditProfileActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    CircleImageView mCircleImageViewBack, mCircleImageProfile;
    ShapeableImageView mImageViewCover;
    TextInputEditText mTextInputEditTextUsername, mTextInputEditTextPhone;
    MaterialButton mButtonEditProfile;
    ImageProvider mImageProvider;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    File mImageFile, mImageFile2, mPhotoFile, mPhotoFile2;
    String mUsername = "", mPhone = "", mImageProfile = "", mImageCover = "", mAbsolutePhotoPath, mPhotoPath, mAbsolutePhotoPath2, mPhotoPath2;
    ProgressDialog progressDialog;
    AlertDialog.Builder mBuilderSelector;
    CharSequence[] options;
    boolean isFirstEditProfile, isFirstEditCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        coordinatorLayout = findViewById(R.id.coordinatorEditProfile);
        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        mCircleImageProfile = findViewById(R.id.circleImageProfile);
        mImageViewCover = findViewById(R.id.imageViewCover);
        mTextInputEditTextUsername = findViewById(R.id.textInputUsername);
        mTextInputEditTextPhone = findViewById(R.id.textInputPhone);
        mButtonEditProfile = findViewById(R.id.btnEditProfile);
        mImageProvider = new ImageProvider();
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Editando perfil...");
        progressDialog.setMessage("Por favor, espere un momento");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mBuilderSelector = new AlertDialog.Builder(this);
        mBuilderSelector.setTitle("Seleccionar imagen");
        options = new CharSequence[]{"Seleccionar de la galería", "Tomar fotografía"};
        validateFieldsAsYouType(mTextInputEditTextUsername, "El nombre de usuario es obligatorio");
        validateFieldsAsYouType(mTextInputEditTextPhone, "El número de teléfono es obligatorio");
        mCircleImageViewBack.setOnClickListener(v -> finish());
        mCircleImageProfile.setOnClickListener(v -> selectOptionsImage());
        mImageViewCover.setOnClickListener(v -> selectOptionsImage2());
        mButtonEditProfile.setOnClickListener(v -> {
            mUsername = Objects.requireNonNull(mTextInputEditTextUsername.getText()).toString().trim();
            mPhone = Objects.requireNonNull(mTextInputEditTextPhone.getText()).toString().trim();
            if (!mUsername.isEmpty() && !mPhone.isEmpty()) {
                if (mImageFile != null && mImageFile2 != null) {
                    saveImageCoverAndProfile(mImageFile, mImageFile2);
                } else if (mPhotoFile != null && mPhotoFile2 != null) {
                    saveImageCoverAndProfile(mPhotoFile, mPhotoFile2);
                } else if (mImageFile != null && mPhotoFile2 != null) {
                    saveImageCoverAndProfile(mImageFile, mPhotoFile2);
                } else if (mPhotoFile != null && mImageFile2 != null) {
                    saveImageCoverAndProfile(mPhotoFile, mImageFile2);
                } else if (mPhotoFile != null) {
                    saveImage(mPhotoFile, true);
                } else if (mPhotoFile2 != null) {
                    saveImage(mPhotoFile2, false);
                } else if (mImageFile != null) {
                    saveImage(mImageFile, true);
                } else if (mImageFile2 != null) {
                    saveImage(mImageFile2, false);
                } else {
                    User user = new User();
                    user.setUsername(mUsername);
                    user.setPhone(mPhone);
                    user.setId(mAuthProvider.getUid());
                    user.setImage_profile(mImageProfile);
                    user.setImage_cover(mImageCover);
                    updateInfo(user);
                }
            } else {
                Snackbar.make(v, "Complete los campos", Snackbar.LENGTH_SHORT).show();
            }
        });
        getUser();
    }

    private void getUser() {
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("username")) {
                    mUsername = documentSnapshot.getString("username");
                    mTextInputEditTextUsername.setText(mUsername);
                }
                if (documentSnapshot.contains("phone")) {
                    mPhone = documentSnapshot.getString("phone");
                    mTextInputEditTextPhone.setText(mPhone);
                }
                if (documentSnapshot.contains("image_profile")) {
                    mImageProfile = documentSnapshot.getString("image_profile");
                    if (mImageProfile != null) {
                        isFirstEditProfile = false;
                        if (!mImageProfile.isEmpty()) {
                            Picasso.get().load(mImageProfile).into(mCircleImageProfile);
                        } else {
                            isFirstEditProfile = true;
                        }
                    } else {
                        isFirstEditProfile = true;
                    }
                }
                if (documentSnapshot.contains("image_cover")) {
                    mImageCover = documentSnapshot.getString("image_cover");
                    if (mImageCover != null) {
                        isFirstEditCover = false;
                        if (!mImageCover.isEmpty()) {
                            Picasso.get().load(mImageCover).into(mImageViewCover);
                        } else {
                            isFirstEditCover = true;
                        }
                    } else {
                        isFirstEditCover = true;
                    }
                }
            }
        });
    }

    private void saveImageCoverAndProfile(File imageFile, File imageFile2) {
        progressDialog.show();
        mImageProvider.save(EditProfileActivity.this, imageFile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    final String urlProfile = uri.toString();
                    mImageProvider.save(EditProfileActivity.this, imageFile2).addOnCompleteListener(taskImage2 -> {
                        if (taskImage2.isSuccessful()) {
                            mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri2 -> {
                                final String urlCover = uri2.toString();
                                User user = new User();
                                user.setUsername(mUsername);
                                user.setPhone(mPhone);
                                if (mImageProfile != null) {
                                    if (!mImageProfile.isEmpty()) {
                                        mImageProvider.deleteFromPath(mImageProfile);
                                    }
                                }
                                user.setImage_profile(urlProfile);
                                if (mImageCover != null) {
                                    if (!mImageCover.isEmpty()) {
                                        mImageProvider.deleteFromPath(mImageCover);
                                    }
                                }
                                user.setImage_cover(urlCover);
                                user.setId(mAuthProvider.getUid());
                                updateInfo(user);
                            });
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(coordinatorLayout, "Error al almacenar la segunda imagen", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                });
            } else {
                progressDialog.dismiss();
                Snackbar.make(coordinatorLayout, "Error al almacenar las imágenes", Snackbar.LENGTH_SHORT).show();
            }
        });
    }


    private void saveImage(File image, boolean isProfileImage) {
        if (!isFirstEditProfile && !isFirstEditCover) {
            progressDialog.show();
            mImageProvider.save(EditProfileActivity.this, image).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        final String url = uri.toString();
                        User user = new User();
                        user.setUsername(mUsername);
                        user.setPhone(mPhone);
                        if (isProfileImage) {
                            mImageProvider.deleteFromPath(mImageProfile);
                            user.setImage_profile(url);
                            user.setImage_cover(mImageCover);
                        } else {
                            mImageProvider.deleteFromPath(mImageCover);
                            user.setImage_cover(url);
                            user.setImage_profile(mImageProfile);
                        }
                        user.setId(mAuthProvider.getUid());
                        updateInfo(user);
                    });
                } else {
                    progressDialog.dismiss();
                    Snackbar.make(coordinatorLayout, "Error al almacenar la imagen", Snackbar.LENGTH_SHORT).show();
                }
            });
        } else {
            Snackbar.make(coordinatorLayout, "Debe establecer imagen de perfil y de portada", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void updateInfo(User user) {
        if (progressDialog.isShowing()) {
            progressDialog.show();
        }
        mUsersProvider.update(user).addOnCompleteListener(task1 -> {
            progressDialog.dismiss();
            if (task1.isSuccessful()) {
                finish();
                Toast.makeText(EditProfileActivity.this, "Perfil editado exitosamente", Toast.LENGTH_SHORT).show();
            } else {
                Snackbar.make(coordinatorLayout, "Error al editar perfil", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void selectOptionsImage() {
        mBuilderSelector.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openGallery();
            } else if (which == 1) {
                takePhoto();
            }
        });
        mBuilderSelector.show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createPhotoFile();
            } catch (Exception e) {
                Snackbar.make(coordinatorLayout, "Error al tomar la primer fotografía", Snackbar.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(EditProfileActivity.this, "com.example.socialmediagamer", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                someActivityResultLauncher3.launch(takePictureIntent);
            }
        }
    }

    private File createPhotoFile() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(new Date() + "_photo", ".jpg", storageDir);
        mPhotoPath = "file:" + photoFile.getAbsolutePath();
        mAbsolutePhotoPath = photoFile.getAbsolutePath();
        return photoFile;
    }

    private void selectOptionsImage2() {
        mBuilderSelector.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openGallery2();
            } else if (which == 1) {
                takePhoto2();
            }
        });
        mBuilderSelector.show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void takePhoto2() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createPhotoFile2();
            } catch (Exception e) {
                Snackbar.make(coordinatorLayout, "Error al tomar la segunda fotografía", Snackbar.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(EditProfileActivity.this, "com.example.socialmediagamer", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                someActivityResultLauncher4.launch(takePictureIntent);
            }
        }
    }

    private File createPhotoFile2() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(new Date() + "_photo", ".jpg", storageDir);
        mPhotoPath2 = "file:" + photoFile.getAbsolutePath();
        mAbsolutePhotoPath2 = photoFile.getAbsolutePath();
        return photoFile;
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            try {
                mPhotoFile = null;
                if (data != null) {
                    mImageFile = FileUtil.from(this, data.getData());
                }
                mCircleImageProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch (Exception e) {
                Snackbar.make(coordinatorLayout, "Error al mostrar la primera imagen", Snackbar.LENGTH_SHORT).show();
            }
        }
    });

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        someActivityResultLauncher.launch(galleryIntent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            try {
                mPhotoFile2 = null;
                if (data != null) {
                    mImageFile2 = FileUtil.from(this, data.getData());
                }
                mImageViewCover.setImageBitmap(BitmapFactory.decodeFile(mImageFile2.getAbsolutePath()));
            } catch (Exception e) {
                Snackbar.make(coordinatorLayout, "Error al mostrar la segunda imagen", Snackbar.LENGTH_SHORT).show();
            }
        }
    });

    private void openGallery2() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        someActivityResultLauncher2.launch(galleryIntent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher3 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mImageFile = null;
            mPhotoFile = new File(mAbsolutePhotoPath);
            Picasso.get().load(mPhotoPath).into(mCircleImageProfile);
        }
    });
    ActivityResultLauncher<Intent> someActivityResultLauncher4 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mImageFile2 = null;
            mPhotoFile2 = new File(mAbsolutePhotoPath2);
            Picasso.get().load(mPhotoPath2).into(mImageViewCover);
        }
    });

    @Override
    protected void onStart() {
        super.onStart();
        ViewedMessageHelper.updateOnline(true, EditProfileActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, EditProfileActivity.this);
    }
}