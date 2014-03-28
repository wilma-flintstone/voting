package my.voting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AccountManager {

	private SharedPreferences file;
	private String fileKey;
	private String accountsKey;

	public AccountManager(Context context) {
		// get strings
		fileKey = context.getString(R.string.accounts_file_key);
		accountsKey = context.getString(R.string.accounts_key);

		// get a handle to the data file
		file = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
	}

	public List<String> getAccounts() {
		List<String> accounts = new ArrayList<String>(file.getStringSet(accountsKey, new HashSet<String>()));
		Collections.sort(accounts);
		return accounts;
	}

	public void addAccount(String username, String password) {
		Set<String> accounts = file.getStringSet(accountsKey, new HashSet<String>());
		accounts.add(username);
		Editor editor = file.edit();
		editor.putStringSet(accountsKey, accounts);
		editor.commit();
		setPassword(username, password);
	}

	public void removeAccount(String username) {
		Set<String> accounts = file.getStringSet(accountsKey, new HashSet<String>());
		accounts.remove(username);
		Editor editor = file.edit();
		editor.putStringSet(accountsKey, accounts);
		editor.remove(username + ".password");
		editor.remove(username + ".cookie");
		editor.commit();
	}

	public String getPassword(String username) {
		return file.getString(username + ".password", "");
	}

	public void setPassword(String username, String password) {
		Editor editor = file.edit();
		editor.putString(username + ".password", password);
		editor.commit();
	}

	public String getCookie(String username) {
		return file.getString(username + ".cookie", "");
	}

	public void setCookie(String username, String cookie) {
		Editor editor = file.edit();
		editor.putString(username + ".cookie", cookie);
		editor.commit();
	}

}
