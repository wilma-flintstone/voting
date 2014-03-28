package my.voting;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DeleteAccounts extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delete_accounts);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// create list view
		List<String> accounts = new AccountManager(getApplicationContext()).getAccounts();
		ListView listView = (ListView) findViewById(R.id.accounts_list);
		String[] arr = new String[accounts.size()];
		accounts.toArray(arr);
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr));
		listView.setOnItemClickListener(new DeleteOnClick());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.delete_accounts, menu);
		return true;
	}

	public class DeleteOnClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			String username = (String) parent.getItemAtPosition(position);
			new AccountManager(getApplicationContext()).removeAccount(username);
			new VoteManager(getApplicationContext()).removeFailedAccount(username);
			startActivity(new Intent(view.getContext(), DeleteAccounts.class));
		}

	}

}
