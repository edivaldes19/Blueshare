package com.example.socialmediagamer.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.models.Post;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.ImageProvider;
import com.example.socialmediagamer.providers.PostProvider;
import com.example.socialmediagamer.utils.FileUtil;
import com.example.socialmediagamer.utils.ViewedMessageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {
    ShapeableImageView mImageViewPost1, mImageViewPost2;
    MaterialCardView mCardViewCulture, mCardViewSport, mCardViewLifestyle, mCardViewMusic, mCardViewProgramation, mCardViewVideogames;
    CircleImageView mCircleImageBack;
    File mImageFile, mImageFile2, mPhotoFile, mPhotoFile2;
    MaterialButton mButtonPost;
    ImageProvider mImageProvider;
    PostProvider mPostProvider;
    AuthProvider mAuthProvider;
    TextInputEditText mTextInputTitle, mTextInputDescription;
    MaterialTextView mTextViewCategory;
    String mTitle = "", mDescription = "", mCategory = "", mAbsolutePhotoPath, mPhotoPath, mAbsolutePhotoPath2, mPhotoPath2;
    ProgressDialog progressDialog;
    AlertDialog.Builder mBuilderSelector;
    CharSequence[] options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mImageViewPost1 = findViewById(R.id.imageViewPost1);
        mImageViewPost2 = findViewById(R.id.imageViewPost2);
        mCardViewCulture = findViewById(R.id.cardViewCulture);
        mCardViewSport = findViewById(R.id.cardViewSport);
        mCardViewLifestyle = findViewById(R.id.cardViewLifestyle);
        mCardViewMusic = findViewById(R.id.cardViewMusic);
        mCardViewProgramation = findViewById(R.id.cardViewProgramation);
        mCardViewVideogames = findViewById(R.id.cardViewVideogames);
        mCircleImageBack = findViewById(R.id.circleImageBack);
        mTextInputTitle = findViewById(R.id.textInputVideogame);
        mTextInputDescription = findViewById(R.id.textInputDescription);
        mTextViewCategory = findViewById(R.id.textViewCategory);
        mButtonPost = findViewById(R.id.btnPost);
        mImageProvider = new ImageProvider();
        mPostProvider = new PostProvider();
        mAuthProvider = new AuthProvider();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Publicando...");
        progressDialog.setMessage("Por favor, espere un momento");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mBuilderSelector = new AlertDialog.Builder(this);
        mBuilderSelector.setTitle("Seleccionar imagen");
        options = new CharSequence[]{"Seleccionar de la galería", "Tomar foto"};
        mCircleImageBack.setOnClickListener(v -> finish());
        mImageViewPost1.setOnClickListener(v -> selectOptionsImage());
        mImageViewPost2.setOnClickListener(v -> selectOptionsImage2());
        mButtonPost.setOnClickListener(v -> {
            mTitle = Objects.requireNonNull(mTextInputTitle.getText()).toString().trim();
            mDescription = Objects.requireNonNull(mTextInputDescription.getText()).toString().trim();
            if (!mTitle.isEmpty() && !mDescription.isEmpty() && !mCategory.isEmpty()) {
                if (mImageFile != null && mImageFile2 != null) {
                    saveImage(mImageFile, mImageFile2);
                } else if (mPhotoFile != null && mPhotoFile2 != null) {
                    saveImage(mPhotoFile, mPhotoFile2);
                } else if (mImageFile != null && mPhotoFile2 != null) {
                    saveImage(mImageFile, mPhotoFile2);
                } else if (mPhotoFile != null && mImageFile2 != null) {
                    saveImage(mPhotoFile, mImageFile2);
                } else {
                    Snackbar.make(v, "Debe seleccionar una imagen", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(v, "Complete los campos", Snackbar.LENGTH_SHORT).show();
            }
        });
        mCardViewCulture.setOnClickListener(v -> {
            mCategory = "Cultura";
            mTextViewCategory.setTextColor(Color.parseColor("#A25918"));
            mTextViewCategory.setText(mCategory);
        });
        mCardViewSport.setOnClickListener(v -> {
            mCategory = "Deporte";
            mTextViewCategory.setTextColor(Color.BLACK);
            mTextViewCategory.setText(mCategory);
        });
        mCardViewLifestyle.setOnClickListener(v -> {
            mCategory = "Estilo de vida";
            mTextViewCategory.setTextColor(Color.parseColor("#A901DB"));
            mTextViewCategory.setText(mCategory);
        });
        mCardViewMusic.setOnClickListener(v -> {
            mCategory = "Música";
            mTextViewCategory.setTextColor(Color.parseColor("#FF0000"));
            mTextViewCategory.setText(mCategory);
        });
        mCardViewProgramation.setOnClickListener(v -> {
            mCategory = "Programación";
            mTextViewCategory.setTextColor(Color.parseColor("#0000FF"));
            mTextViewCategory.setText(mCategory);
        });
        mCardViewVideogames.setOnClickListener(v -> {
            mCategory = "Videojuegos";
            mTextViewCategory.setTextColor(Color.parseColor("#008000"));
            mTextViewCategory.setText(mCategory);
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
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(PostActivity.this, "com.example.socialmediagamer", photoFile);
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
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(PostActivity.this, "com.example.socialmediagamer", photoFile);
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

    private void saveImage(File imageFile, File imageFile2) {
        progressDialog.show();
        mImageProvider.save(PostActivity.this, imageFile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    final String url = uri.toString();
                    mImageProvider.save(PostActivity.this, imageFile2).addOnCompleteListener(taskImage2 -> {
                        if (taskImage2.isSuccessful()) {
                            mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri2 -> {
                                final String url2 = uri2.toString();
                                Post post = new Post();
                                post.setImage1(url);
                                post.setImage2(url2);
                                post.setTitle(mTitle);
                                post.setDescription(mDescription);
                                post.setCategory(mCategory);
                                post.setIdUser(mAuthProvider.getUid());
                                post.setTimestamp(new Date().getTime());
                                mPostProvider.save(post).addOnCompleteListener(taskSave -> {
                                    progressDialog.dismiss();
                                    if (taskSave.isSuccessful()) {
                                        clearForm();
                                        Toast.makeText(PostActivity.this, "Publicación creada exitosamente", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(PostActivity.this, "Error al crear publicación", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(PostActivity.this, "Error al almacenar la segunda imagen", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } else {
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, "Error al almacenar la imagen en la base de datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        mTextInputTitle.setText(null);
        mTextInputDescription.setText(null);
        mTextViewCategory.setText(null);
        mImageViewPost1.setImageResource(R.drawable.ic_insert_photo);
        mImageViewPost2.setImageResource(R.drawable.ic_insert_photo);
        mTitle = "";
        mDescription = "";
        mCategory = "";
        mImageFile = null;
        mImageFile2 = null;
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            try {
                mPhotoFile = null;
                assert data != null;
                mImageFile = FileUtil.from(this, data.getData());
                mImageViewPost1.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch (Exception e) {
                Toast.makeText(this, "Error al mostrar la imagen" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    });

    public void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        someActivityResultLauncher.launch(galleryIntent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            try {
                mPhotoFile2 = null;
                assert data != null;
                mImageFile2 = FileUtil.from(this, data.getData());
                mImageViewPost2.setImageBitmap(BitmapFactory.decodeFile(mImageFile2.getAbsolutePath()));
            } catch (Exception e) {
                Toast.makeText(this, "Error al mostrar la imagen" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    });

    public void openGallery2() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        someActivityResultLauncher2.launch(galleryIntent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher3 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mImageFile = null;
            mPhotoFile = new File(mAbsolutePhotoPath);
            Picasso.with(PostActivity.this).load(mPhotoPath).into(mImageViewPost1);
        }
    });

    ActivityResultLauncher<Intent> someActivityResultLauncher4 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mImageFile2 = null;
            mPhotoFile2 = new File(mAbsolutePhotoPath2);
            Picasso.with(PostActivity.this).load(mPhotoPath2).into(mImageViewPost2);
        }
    });

    @Override
    protected void onStart() {
        super.onStart();
        ViewedMessageHelper.updateOnline(true, PostActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, PostActivity.this);
    }
}