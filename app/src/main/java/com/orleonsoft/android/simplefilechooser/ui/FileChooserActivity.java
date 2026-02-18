package com.orleonsoft.android.simplefilechooser.ui;

import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.triops.R;
import com.orleonsoft.android.simplefilechooser.Constants;
import com.orleonsoft.android.simplefilechooser.FileInfo;
import com.orleonsoft.android.simplefilechooser.adapters.FileArrayAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileChooserActivity extends AppCompatActivity {

    private ListView listView;
	private File currentFolder = null;
	private FileArrayAdapter fileArrayListAdapter;
	private FileFilter fileFilter;
	private File fileSelected;
	private ArrayList<String> extensions;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // secure window against automatic screenshots from the recent-tasks history.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        // ListActivity used a default layout or android.R.id.list.
        // Since you are using a custom adapter, you should set a content view
        // or create the ListView programmatically.
        // Assuming you want to keep it simple:
        listView = new ListView(this);
        setContentView(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // This calls your helper method below
                handleListItemClick((ListView) parent, view, position, id);
            }
        });

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras
					.getStringArrayList(Constants.KEY_FILTER_FILES_EXTENSIONS) != null) {
				extensions = extras
						.getStringArrayList(Constants.KEY_FILTER_FILES_EXTENSIONS);
				fileFilter = new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return ((pathname.isDirectory()) ||
								(pathname.getName().contains(".") &&
										extensions.contains(
											pathname.getName().substring(
												pathname.getName().lastIndexOf("."))
										)
								)
						);
					}
				};
			}
		}
		setInitialDirectory();
		fill(currentFolder);

		// show brief instructions for multiple selections with checkboxes:
		if (extras != null &&
				extras.getString(Constants.KEY_SHOW_CHECKBOXES_FOR_FILES) != null)
			Toast.makeText(
				this,
				"You can check multiple files and then validate the choice by selecting any",
				Toast.LENGTH_LONG
			).show();

	}

	private void setInitialDirectory() {
		Intent thisInt = this.getIntent();
		String requestedStartDir = thisInt
				.getStringExtra(Constants.KEY_INITIAL_DIRECTORY);

		if (requestedStartDir != null && requestedStartDir.length() > 0) {
			File tempFile = new File(requestedStartDir);
			if (tempFile.isDirectory())
				this.currentFolder = tempFile;
		}

		if (this.currentFolder == null) {// No or invalid directory supplied in intent parameter
			if (Environment.getExternalStorageDirectory().isDirectory()
					&& Environment.getExternalStorageDirectory().canRead())
				currentFolder = Environment.getExternalStorageDirectory();
			else
				currentFolder = new File("/");
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			/*if ((!currentFolder.getName().equals(
					Environment.getExternalStorageDirectory().getName()))
					&& (currentFolder.getParentFile() != null)) {
				currentFolder = currentFolder.getParentFile();
				fill(currentFolder);
			} else {*/
				//Log.i("FILE CHOOSER", "canceled");
				setResult(Activity.RESULT_CANCELED);				
				finish();
			/*}*/
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

    /**
     * Converts bytes into a human-readable format (e.g., 5.3 MiB).
     * @param bytes The file size in bytes.
     * @return A formatted string.
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        // Define the units
        String[] units = {"bytes", "KiB", "MiB", "GiB", "TiB", "PiB"};

        // Calculate the index in the units array
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        // Return formatted string with 1 decimal place
        return String.format(Locale.getDefault(), "%.1f %s",
                bytes / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }

	private void fill(File f) {
		File[] folders = null;
		if (fileFilter != null)
			folders = f.listFiles(fileFilter);
		else
			folders = f.listFiles();

		this.setTitle(getString(R.string.currentDir) + ": " + f.getName());
		List<FileInfo> dirs = new ArrayList<FileInfo>();
		List<FileInfo> files = new ArrayList<FileInfo>();
		try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
			for (File file : folders) {
				if (file.isDirectory() && !file.isHidden())
					//si es un directorio en el data ponemos la constante folder
					dirs.add(new FileInfo(file.getName(),
							Constants.FOLDER, file.getAbsolutePath(),
							true, false, false));
				else {
					if (!file.isHidden()) {
                        // Format the last modified date
                        String dateStr = sdf.format(new java.util.Date(file.lastModified()));

                        files.add(new FileInfo(file.getName(),
                                dateStr + ", " + formatFileSize(file.length()),
                                file.getAbsolutePath(), false, false, true));
                    }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(dirs);
		Collections.sort(files);
		dirs.addAll(files);
		/*if (!f.getName().equalsIgnoreCase(
				Environment.getExternalStorageDirectory().getName())) {*/
			if (f.getParentFile() != null)
			//si es un directorio padre en el data ponemos la constante adeacuada
				dirs.add(0, new FileInfo(Constants.PARENT_FOLDER,
						f.getAbsolutePath(), f.getParent(),
						false, true, false));
		/*}*/
		fileArrayListAdapter = new FileArrayAdapter(this,
				R.layout.file_row, dirs);
        listView.setAdapter(fileArrayListAdapter);
	}

	protected void handleListItemClick(ListView l, View v, int position, long id) {

		ArrayList<String> list_of_selected_files = new ArrayList<String>();

		// implements checkable items
		// Extract all files checked, if any:
		List<FileInfo> elements = fileArrayListAdapter.getCheckedItems();
		if (elements.size() != 0) {
			// there're files checked:
			// return now with them as result:
			for (FileInfo element : elements){
				fileSelected = new File(element.getPath());
				list_of_selected_files.add( fileSelected.getAbsolutePath() );
			}
			// pass array of files to MainActivity:
			Intent intent = new Intent();
			intent.putStringArrayListExtra(Constants.KEY_FILE_SELECTED, list_of_selected_files);
			setResult(Activity.RESULT_OK, intent);
			//Log.i("FILE CHOOSER", "result ok");
			finish();

		} else {
			// no file checked, but one file selected:

			FileInfo fileDescriptor = fileArrayListAdapter.getItem(position);

			if (fileDescriptor.isFolder() || fileDescriptor.isParent()) {
				currentFolder = new File(fileDescriptor.getPath());
				fill(currentFolder);
			} else {

				fileSelected = new File(fileDescriptor.getPath());
				list_of_selected_files.add(fileSelected.getAbsolutePath());
				// pass array of 1 file to MainActivity:
				Intent intent = new Intent();
				intent.putStringArrayListExtra(Constants.KEY_FILE_SELECTED, list_of_selected_files);
				setResult(Activity.RESULT_OK, intent);
				//Log.i("FILE CHOOSER", "result ok");
				finish();
			}

		}
	}

}