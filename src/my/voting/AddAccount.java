package my.voting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class AddAccount extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_account);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_account, menu);
		return true;
	}

	public void add(View view) {
		String username = ((EditText) findViewById(R.id.username)).getText().toString();
		String password = ((EditText) findViewById(R.id.password)).getText().toString();
		new AccountManager(getApplicationContext()).addAccount(username, password);
		startActivity(new Intent(this, Vote.class));
	}

}
