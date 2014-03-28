package my.voting;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AccountDetails extends Activity {

	private Context context;
	private AccountManager accountManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_details);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// set private variables
		context = this;
		accountManager = new AccountManager(getApplicationContext());

		// create list view
		List<String> accounts = accountManager.getAccounts();
		ListView listView = (ListView) findViewById(R.id.accounts_list);
		String[] arr = new String[accounts.size()];
		accounts.toArray(arr);
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr));
		listView.setOnItemClickListener(new DetailsOnClick());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.account_details, menu);
		return true;
	}

	public class DetailsOnClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			String username = (String) parent.getItemAtPosition(position);
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(getString(R.string.detail_title))
					.setMessage(
							getString(R.string.detail_username) + ": " + username + "\n"
									+ getString(R.string.detail_password) + ": " + accountManager.getPassword(username))
					.setPositiveButton(getString(R.string.detail_ok), null);
			builder.show();
		}

	}

}
