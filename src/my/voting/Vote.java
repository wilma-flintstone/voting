package my.voting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class Vote extends Activity {

	public static final String DEBUG = "debug";
	public static final String ERROR = "error";

	private AccountManager accountManager;
	private VoteManager voteManager;
	private NetworkManager networkManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vote);

		// init managers
		accountManager = new AccountManager(getApplicationContext());
		voteManager = new VoteManager(getApplicationContext());
		networkManager = new NetworkManager(getApplicationContext());

		// get time of last vote and update corresponding text view
		((TextView) findViewById(R.id.last_voted)).setText(voteManager.getLastVote());

		// get failed accounts and update corresponding text view
		printFailedAccounts();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.vote, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_account_details:
			startActivity(new Intent(this, AccountDetails.class));
			return true;
		case R.id.action_add_account:
			startActivity(new Intent(this, AddAccount.class));
			return true;
		case R.id.action_delete_accounts:
			startActivity(new Intent(this, DeleteAccounts.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void clearFailed(View view) {
		voteManager.clearFailedAccounts();
		((TextView) findViewById(R.id.failed_accounts)).setText("Failed accounts: None");
	}

	public void vote(View view) {
		List<String> accounts;
		boolean aborting = false;

		// if there are accounts where voting failed, try again for those, otherwise vote for all accounts
		List<String> failedAccounts = voteManager.getFailedAccounts();
		if (failedAccounts.size() > 0) {
			accounts = failedAccounts;
		} else {
			accounts = accountManager.getAccounts();
		}

		// vote for each account
		for (String account : accounts) {
			if (aborting) {
				Log.d(DEBUG, "Aborting voting for account '" + account + "'.");
				voteManager.addFailedAccount(account);
				continue;
			}

			// connect to mobile data service
			Log.d(DEBUG, "Connecting...");
			if (!networkManager.connect()) {
				Log.d(DEBUG, "Account '" + account + "' failed to connect.");
				voteManager.addFailedAccount(account);
				continue;
			}
			Log.d(DEBUG, "Connected.");

			// login
			Log.d(DEBUG, "Logging in...");
			LoginTask login = new LoginTask();
			login.execute(getString(R.string.login_url), account);
			try {
				String errorMessage = login.get(5, TimeUnit.SECONDS);
				if (errorMessage != null) {
					Log.e(ERROR, "Failed to login: " + errorMessage);
					Log.d(DEBUG, "Account '" + account + "' failed to login.");
					voteManager.addFailedAccount(account);
					continue;
				}
			} catch (Exception e) {
				Log.e(ERROR, "Failed to wait for login task: " + e.getMessage());
				Log.d(DEBUG, "Account '" + account + "' failed to login.");
				voteManager.addFailedAccount(account);
				continue;
			}
			Log.d(DEBUG, "Login successful.");

			// navigate to vote site
			Log.d(DEBUG, "Navigating to vote site...");
			NavigateTask navigate = new NavigateTask();
			navigate.execute(getString(R.string.vote_url), account);
			try {
				String errorMessage = navigate.get(5, TimeUnit.SECONDS);
				if (errorMessage != null) {
					Log.e(ERROR, "Failed to navigate: " + errorMessage);
					Log.d(DEBUG, "Account '" + account + "' failed to navigate.");
					voteManager.addFailedAccount(account);
					continue;
				}
			} catch (Exception e) {
				Log.e(ERROR, "Failed to wait for navigate task: " + e.getMessage());
				Log.d(DEBUG, "Account '" + account + "' failed to navigate.");
				voteManager.addFailedAccount(account);
				continue;
			}
			Log.d(DEBUG, "Navigating successful.");

			// vote
			VoteTask[] voteTasks = new VoteTask[3];
			for (int i = 0; i < 3; i++) {
				voteTasks[i] = new VoteTask();
				voteTasks[i].execute(getString(R.string.vote_base_url), account, Integer.toString(i + 1));
			}
			boolean votingSuccessful = true;
			for (int i = 0; i < 3; i++) {
				try {
					String errorMessage = voteTasks[i].get(5, TimeUnit.SECONDS);
					if (errorMessage != null) {
						Log.e(ERROR, "Failed to vote: " + errorMessage);
						Log.d(DEBUG, "Vote task " + i + " failed for account '" + account + "'.");
						voteManager.addFailedAccount(account);
						votingSuccessful = false;
						continue;
					}
				} catch (Exception e) {
					Log.e(ERROR, "Failed to wait for vote task " + i + ": " + e.getMessage());
					Log.d(DEBUG, "Vote task " + i + " failed for account '" + account + "'.");
					voteManager.addFailedAccount(account);
					votingSuccessful = false;
					continue;
				}
			}
			if (votingSuccessful) {
				Log.d(DEBUG, "Voting successful.");
			}

			// disconnect from mobile data service
			Log.d(DEBUG, "Disconnecting...");
			if (!networkManager.disconnect()) {
				Log.e(ERROR, "Failed to disconnect. Aborting further voting.");
				aborting = true;
				continue;
			}
			Log.d(DEBUG, "Disconnected.");
			if (votingSuccessful) {
				voteManager.removeFailedAccount(account);
			}
		}

		// update last voted timestamp
		String time = voteManager.setLastVoted();
		((TextView) findViewById(R.id.last_voted)).setText(time);

		// update failed accounts
		printFailedAccounts();
	}

	private void printFailedAccounts() {
		StringBuilder builder = new StringBuilder();
		List<String> failedAccounts = new ArrayList<String>(voteManager.getFailedAccounts());
		Collections.sort(failedAccounts);
		for (String account : failedAccounts) {
			builder.append(account + ", ");
		}
		String failed;
		if (builder.length() > 1) {
			failed = builder.substring(0, builder.length() - 2);
		} else {
			failed = "None";
		}
		((TextView) findViewById(R.id.failed_accounts)).setText("Failed accounts: " + failed);
	}

	private class LoginTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String username = params[1];
			String password = accountManager.getPassword(username);
			String requestParams = null;
			try {
				requestParams = "username=" + URLEncoder.encode(username, "UTF-8") + "&password="
						+ URLEncoder.encode(password, "UTF-8") + "&submit=Login";
			} catch (UnsupportedEncodingException e) {
				return e.getMessage();
			}

			try {
				URL url = new URL(params[0]);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("User-Agent", getString(R.string.user_agent));
				conn.setRequestProperty("Referer", getString(R.string.login_url));
				conn.setDoOutput(true);
				conn.setFixedLengthStreamingMode(requestParams.getBytes().length);
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				PrintWriter out = new PrintWriter(conn.getOutputStream());
				out.print(requestParams);
				out.close();

				Map<String, List<String>> headerFields = conn.getHeaderFields();
				String[] headerEntries = headerFields.get("Set-Cookie").get(0).split(";");
				String cookie = null;
				for (String headerEntry : headerEntries) {
					if (headerEntry.startsWith("PHPSESSID")) {
						cookie = headerEntry;
					}
				}
				accountManager.setCookie(username, cookie);
			} catch (IOException e) {
				return e.getMessage();
			}
			return null;
		}
	}

	private class NavigateTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String username = params[1];
			String cookie = accountManager.getCookie(username);
			try {
				URL url = new URL(params[0]);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Cookie", cookie);
				conn.setRequestProperty("User-Agent", getString(R.string.user_agent));
				conn.setRequestProperty("Referer", getString(R.string.home_url));
				conn.setDoInput(true);
				conn.connect();
				int response = conn.getResponseCode();
				if (response != 200) {
					Log.e(ERROR, "Response code for navigating task: " + response);
					return "Unexpected response code.";
				}
			} catch (IOException e) {
				return e.getMessage();
			}
			return null;
		}
	}

	private class VoteTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String username = params[1];
			String cookie = accountManager.getCookie(username);
			try {
				URL url = new URL(params[0] + "/" + params[2]);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Cookie", cookie);
				conn.setRequestProperty("User-Agent", getString(R.string.user_agent));
				conn.setRequestProperty("Referer", getString(R.string.vote_url));
				conn.setDoInput(true);
				conn.connect();
				int response = conn.getResponseCode();
				if (response != 302) {
					Log.e(ERROR, "Response code: " + response);
					InputStream is = conn.getInputStream();
					Log.d(DEBUG, readIt(is, 15000));
					return "Unexpected response code.";
				}
			} catch (IOException e) {
				return e.getMessage();
			}
			return null;
		}

		// Reads an InputStream and converts it to a String.
		public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
			Reader reader = null;
			reader = new InputStreamReader(stream, "UTF-8");
			char[] buffer = new char[len];
			reader.read(buffer);
			return new String(buffer);
		}
	}

}
