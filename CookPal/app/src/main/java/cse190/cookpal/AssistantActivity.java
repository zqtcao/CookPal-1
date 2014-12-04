package cse190.cookpal;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import com.google.android.gms.analytics.GoogleAnalytics;


public class AssistantActivity extends BaseDrawerActivity {

    private RelativeLayout stepListLayout;

    private TextView stepNumView;
    private TextView stepTitleView;
    private TextView stepDescriptView;
    private ListView stepListView;

    private ListAdapter stepListAdapter;

    private Recipe currRecipe;
    private Step currStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);
        ((CookPalApp) getApplication()).getTracker(CookPalApp.TrackerName.APP_TRACKER);
        // Hide the step list on the bottom (the ETC bar) until it's clicked
        stepListLayout = (RelativeLayout) findViewById(R.id.assistant_stepListLayout);
        stepListLayout.setVisibility(View.GONE);

        // Recipe creation
        //TODO: pull in recipe class from Intent.getIntent()? something like that.
        currRecipe = new Recipe("Chicken and Rice");

        if(null != currRecipe.getStepList()) {
            currStep = currRecipe.getStepList().get(0);
        }

        // Bind and populate the step information
        stepNumView = (TextView) findViewById(R.id.assistant_stepNumber);
        stepTitleView = (TextView) findViewById(R.id.assistant_stepTitle);
        stepDescriptView = (TextView) findViewById(R.id.assistant_stepDescription);
        stepListView = (ListView) findViewById(R.id.assistant_stepListView);

        stepNumView.setText(currStep.getStepNumber() + "");
        stepTitleView.setText(currStep.getTitle());
        stepDescriptView.setText(currStep.getDescription());

        // Bind the step list adapter
        stepListAdapter = new ArrayAdapter<Step>(this, android.R.layout.simple_list_item_1, currRecipe.getStepList());
        stepListView.setAdapter(stepListAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.assistant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void stepListToggle(View view) {
        if(stepListLayout.getVisibility() == View.GONE) {
            stepListLayout.setVisibility(View.VISIBLE);
        } else {
            stepListLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //start tracking
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        //stop tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);

    }
}
