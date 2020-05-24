package com.example.workoutapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DurationDialog.DurationDialogListener{

    //Activity Database
    private SQLiteDatabase mDatabase;

    private ActivityAdapter mAdapter;

    //Duration of an Activity
    private EditText setDuration;
    //Name of an Activity
    private EditText mActivityName;
    //Add button for adding an activity in the database
    private Button buttonAdd;
    //Edit button for deleting and swapping activities in the database
    private Button buttonEdit;
    //Active activity text view
    private TextView mActiveActivity;

    //Variable indicating whether the edit button has been pressed
    private boolean isEdit = false;

    //Timer variables
    private TextView mTextViewCountDown;
    private TextView mTextViewCountDownActivity;
    private Button mButtonStartPause;
    private Button mButtonReset;

    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;

    //input of the Duration Dialog
    public long input;
    //initial time to be set on start of the app
    public long initialTime = 0;
    //Variable to know when we pressed the ok of the Duration dialog and are waiting for the Add
    //button to be pressed
    public long beforeAdd = 0;

    //Is the Activity and Duration values to be add invalid (either null)
    private boolean isInvalid = false;

    private long duration = 0;

    ActivityDBHelper dbHelper = new ActivityDBHelper(this);

    public LinkedHashMap<Integer, List<String>> map = new LinkedHashMap();
    public List<String> l;
    //Duration of the current activity that is running
    public long activityDuration;
    //key in the map of the current activity that is running
    public int currentExercise;
    public boolean isOnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Timer variables
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mTextViewCountDownActivity = findViewById(R.id.text_view_countdown_activity);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_reset);

        //Edit button for deleting and swapping activities in the database
        buttonEdit = findViewById(R.id.button_edit);

        //Activity database
        mDatabase = dbHelper.getWritableDatabase();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ActivityAdapter(this, getAllItems());
        recyclerView.setAdapter(mAdapter);

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editText = buttonEdit.getText().toString();
                if (editText == "Edit") {
                    isEdit = true;
                    buttonEdit.setText("Done");
                } else {
                    isEdit = false;
                    buttonEdit.setText("Edit");
                }
            }
        });

        //Deleting rows from the database with item touch helper
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT |
                ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                removeItem((long) viewHolder.itemView.getTag());
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                //return true;
                if (isEdit == true) {
                    return true;
                } else {
                    return false;
                }
            }
        }).attachToRecyclerView(recyclerView);

        //Duration of an Activity
        setDuration = findViewById(R.id.setDuration);
        //Name of an Activity
        mActivityName = findViewById(R.id.setActivityName);
        //Add button for adding an activity in the database
        buttonAdd = findViewById(R.id.button_add);
        //Active Activity text view
        mActiveActivity = findViewById(R.id.activeActivity);

        setDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
                if (isInvalid == false) {
                    if (mTimerRunning) {
                        pauseTimer();
                    }
                    if (mTimeLeftInMillis == 0) {
                        setTime(beforeAdd);
                    } else {
                        addTime(beforeAdd);
                    }
                } else {
                    isInvalid = false;
                }
                beforeAdd = 0;
                input = 0;
            }
        });

        mButtonStartPause.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View v){
                if(mTimerRunning) {
                    pauseTimer();
                } else {
                    if (mTimeLeftInMillis == setInitialTime()) {
                        //Delay the start of the countdown timer with a second if it has not been paused
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startTimer();
                            }
                        }, 1000);
                    } else {
                        startTimer();
                    }
                    //Whenever the workout is in progress, the user cannot add and edit activities
                    buttonAdd.setEnabled(false);
                    buttonEdit.setEnabled(false);
                }
                if (currentExercise == 0 && isOnStart == true) {
                    l = map.get(0);
                    activityDuration = Long.parseLong(l.get(1));
                    mTextViewCountDownActivity.setText(formatDuration(activityDuration));
                    mActiveActivity.setText(l.get(0));
                    isOnStart = false;
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View v){
                //Whenever the user resets the workout, it is again possible to add and edit activities
                buttonEdit.setEnabled(true);
                buttonAdd.setEnabled(true);
                resetTimer();
            }
        });

        updateCountDownText();

        //Set the time on the timer to be equal to the sum of the activities in the database
        setTime(setInitialTime());

        //The database converted to a linked map
        map = dbHelper.databaseToString();
        currentExercise = 0;
        isOnStart = true;
    }

    private void setTextView() {
        if (activityDuration == 1000) {
            currentExercise = currentExercise + 1;
            l = map.get(currentExercise);
            activityDuration = Long.parseLong(l.get(1));
            mTextViewCountDownActivity.setText(formatDuration(activityDuration));
            mActiveActivity.setText(l.get(0));
        } else {
            activityDuration = activityDuration - 1000;
            updateCountDownTextActivity();
        }
    }

    private void openDialog() {
        DurationDialog durationDialog = new DurationDialog();
        durationDialog.show(getSupportFragmentManager(), "Duration Dialog");
    }

    @Override
    public void applyDurationInput (int durationInput) {
        input = durationInput;
        long millisInput = input * 60000 / 60;

        setDuration.setText(formatDuration(millisInput), TextView.BufferType.EDITABLE);

        if (millisInput == 0) {
            Toast.makeText(MainActivity.this, "Duration should be positive", Toast.LENGTH_SHORT).show();
            return;
        }

        //Waiting for the add button to be pressed in order to change the time on the timer
        beforeAdd = millisInput;
    }

    //Add an item to the database
    private void addItem() {

        if (mActivityName.getText().toString().trim().length() == 0 || input == 0) {
            Toast.makeText(MainActivity.this, "Activity and Duration should be both filled", Toast.LENGTH_SHORT).show();
            isInvalid = true;
            mActivityName.getText().clear();
            setDuration.getText().clear();
            return;
        }

        String name = mActivityName.getText().toString();
        ContentValues cv = new ContentValues();
        cv.put(Activity.ActivityEntry.COLUMN_NAME, name);
        //Activity duration (the input) in milliseconds
        cv.put(Activity.ActivityEntry.COLUMN_AMOUNT, input * 60000 / 60);

        mDatabase.insert(Activity.ActivityEntry.TABLE_NAME, null, cv);
        mAdapter.swapCursor(getAllItems());

        mActivityName.getText().clear();
        setDuration.getText().clear();

        //Update map of activities after the user has add an activity
        map = dbHelper.databaseToString();
    }

    //Get the duration of an element with a given id
    public long getDuration(long id) {
        String query = "SELECT " + Activity.ActivityEntry.COLUMN_AMOUNT + " FROM " + Activity.ActivityEntry.TABLE_NAME +
                " WHERE " + Activity.ActivityEntry._ID + " = " + id;
        Cursor c = mDatabase.rawQuery(query,null);

        if(c.moveToFirst()) {
            duration = c.getLong(0);
        }
        c.close();
        return duration;
    }

    //Remove an item from the database and subtract the time on the timer
    private void removeItem(long id) {
        long subtractFromTimer = getDuration(id);
        subtractTime(subtractFromTimer);
        mDatabase.delete(Activity.ActivityEntry.TABLE_NAME,
                Activity.ActivityEntry._ID + "=" + id, null);

        mAdapter.swapCursor(getAllItems());
        // Update map when an item is deleted from the databse
        map = dbHelper.databaseToString();
    }

    private Cursor getAllItems() {
        return mDatabase.query(
                Activity.ActivityEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                Activity.ActivityEntry.COLUMN_TIMESTAMP + " ASC"
        );
    }

    //Set time on timer on Start of the App to be the sum of the activities in the database
    private long setInitialTime () {
        String query = "SELECT SUM(" + Activity.ActivityEntry.COLUMN_AMOUNT + ") FROM " + Activity.ActivityEntry.TABLE_NAME;
        Cursor c = mDatabase.rawQuery(query,null);

        if(c.moveToFirst()) {
             initialTime = c.getLong(0);
        }
        c.close();
        return initialTime;
    }

    //Set the time on the timer
    private void setTime(long milliseconds) {
        mStartTimeInMillis = milliseconds;
        //resetTimer();
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
    }

    //Add to the timer specific milliseconds of time
    private void addTime(long milliseconds) {
        mStartTimeInMillis = mTimeLeftInMillis + milliseconds;
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
    }

    //Subtract from the timer specific milliseconds of time
    private void subtractTime(long milliseconds) {
        mStartTimeInMillis = mTimeLeftInMillis - milliseconds;
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
    }

    private void startTimer(){
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                if (mTimeLeftInMillis > 1000) {
                    setTextView();
                } else {
                    activityDuration = 0;
                    mTextViewCountDownActivity.setText(formatDuration(activityDuration));
                    mActiveActivity.setText("");
                    isOnStart = true;
                }
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                mButtonStartPause.setText("Start");
                //Whenever the user completes the workout, it is again possible to add and edit activities
                buttonEdit.setEnabled(true);
                buttonAdd.setEnabled(true);
                mActiveActivity.setText("You finished!");
            }
        }.start();

        mTimerRunning = true;
        mButtonStartPause.setText("Pause");
    }
    private void pauseTimer(){
        mCountDownTimer.cancel();
        mTimerRunning = false;
        mButtonStartPause.setText("Start");
        activityDuration = activityDuration + 1000;
    }
    private void resetTimer(){
        pauseTimer();
        //mTimeLeftInMillis = mStartTimeInMillis;
        mTimeLeftInMillis = setInitialTime();
        setTime(mTimeLeftInMillis);
        updateCountDownText();
        currentExercise = 0;
        mTextViewCountDownActivity.setText("00:00");
        mActiveActivity.setText("");
        isOnStart = true;
    }

    private void updateCountDownText() {
        int hours = (int) mTimeLeftInMillis / 1000 / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) mTimeLeftInMillis / 1000 % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes,seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes,seconds);
        }
        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void updateCountDownTextActivity() {
        int hours = (int) activityDuration / 1000 / 3600;
        int minutes = (int) ((activityDuration / 1000) % 3600) / 60;
        int seconds = (int) activityDuration / 1000 % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes,seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes,seconds);
        }
        mTextViewCountDownActivity.setText(timeLeftFormatted);
    }

    private String formatDuration (long duration) {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;

        String formatted = String.format(Locale.getDefault(),
                "%02d:%02d", minutes, seconds);

        return formatted;
    }

}


