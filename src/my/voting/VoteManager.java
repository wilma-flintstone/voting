package my.voting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class VoteManager {

	private SharedPreferences file;
	private String fileKey;
	private String failedKey;
	private String lastVoteKey;
	private Set<String> failedAccounts;

	public VoteManager(Context context) {
		// get strings
		fileKey = context.getString(R.string.accounts_file_key);
		failedKey = context.getString(R.string.failed_key);
		lastVoteKey = context.getString(R.string.last_vote_key);

		// get a handle to the data file
		file = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);

		// load failed accounts
		failedAccounts = file.getStringSet(failedKey, new HashSet<String>());
	}

	public List<String> getFailedAccounts() {
		List<String> failed = new ArrayList<String>(failedAccounts);
		Collections.sort(failed);
		return failed;
	}

	public void setFailedAccounts(Set<String> failedAccounts) {
		Editor editor = file.edit();
		editor.putStringSet(failedKey, failedAccounts);
		editor.commit();
		this.failedAccounts = failedAccounts;
	}

	public void addFailedAccount(String username) {
		if (!failedAccounts.contains(username)) {
			failedAccounts.add(username);
			setFailedAccounts(failedAccounts);
		}
	}

	public void removeFailedAccount(String username) {
		if (failedAccounts.contains(username)) {
			failedAccounts.remove(username);
			setFailedAccounts(failedAccounts);
		}
	}

	public void clearFailedAccounts() {
		setFailedAccounts(new HashSet<String>());
	}

	public String getLastVote() {
		return new Date(file.getLong(lastVoteKey, 0)).toString();
	}

	public String setLastVoted() {
		Editor editor = file.edit();
		long curTime = new Date().getTime();
		editor.putLong(lastVoteKey, curTime);
		editor.commit();
		return new Date(curTime).toString();
	}

}
