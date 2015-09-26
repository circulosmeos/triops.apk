package com.orleonsoft.android.simplefilechooser.ui;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.orleonsoft.android.simplefilechooser.Constants;
import com.orleonsoft.android.simplefilechooser.FileInfo;
import com.example.triops.R;
import com.orleonsoft.android.simplefilechooser.adapters.FileArrayAdapter;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;

public class FileChooserActivity extends ListActivity {

	private File currentFolder = null;
	private FileArrayAdapter fileArrayListAdapter;
	private FileFilter fileFilter;
	private File fileSelected;
	private ArrayList<String> extensions;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
				Log.i("FILE CHOOSER", "canceled");
				setResult(Activity.RESULT_CANCELED);				
				finish();
			/*}*/
			return false;
		}
		return super.onKeyDown(keyCode, event);
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
			for (File file : folders) {
				if (file.isDirectory() && !file.isHidden())
					//si es un directorio en el data ponemos la constante folder
					dirs.add(new FileInfo(file.getName(),
							Constants.FOLDER, file.getAbsolutePath(),
							true, false));
				else {
					if (!file.isHidden())
						files.add(new FileInfo(file.getName(),
								getString(R.string.fileSize) + ": "
										+ file.length(),
								file.getAbsolutePath(), false, false));
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
						false, true));
		/*}*/
		fileArrayListAdapter = new FileArrayAdapter(FileChooserActivity.this,
				R.layout.file_row, dirs);
		this.setListAdapter(fileArrayListAdapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		FileInfo fileDescriptor = fileArrayListAdapter.getItem(position);
		if (fileDescriptor.isFolder() || fileDescriptor.isParent()) {
			currentFolder = new File(fileDescriptor.getPath());
			fill(currentFolder);
		} else {

			fileSelected = new File(fileDescriptor.getPath());
			Intent intent = new Intent();
			intent.putExtra(Constants.KEY_FILE_SELECTED,
					fileSelected.getAbsolutePath());
			setResult(Activity.RESULT_OK, intent);
			Log.i("FILE CHOOSER", "result ok");
			finish();
		}
	}

}