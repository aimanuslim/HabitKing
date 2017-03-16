package com.theunheard.habitking;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;
import static android.text.InputType.TYPE_CLASS_DATETIME;
import static android.text.InputType.TYPE_DATETIME_VARIATION_DATE;


public class InsertDataFragment extends Fragment {

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    private AutoCompleteTextView habitNameTextView;
    private EditText dateTextView;
    private EditText timeTextView;
    private AutoCompleteTextView categoryTextView;
    private EditText repetitionFrequencyTextView;
    private Spinner repetitionPeriodSpinner;
    private ListView personInteractedListView;

    private Button addPersonButton;
    private Button recordButton;
    private Button nowButton;

    private DBHandler _dbHandler;

    private String[] periodArray;

    // Firebase objects
    private DatabaseReference _databaseRef;
    private FirebaseDatabase _firebaseInstance;

    private final static String TAG = "HabitKing";

    // for list view
    private ArrayList<String>  person_list;
    ArrayAdapter<String> arrayAdapter;

    public InsertDataFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insert_data, container, false);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAll();
    }

    private void setupAll() {

        this.periodArray = new String[] {"Minute (s)", "Hour (s)", "Day (s)", "Week (s)", "Month (s)", "Year (s)"};


        habitNameTextView = (AutoCompleteTextView) getView().findViewById(R.id.habitInputName);
        dateTextView = (EditText) getView().findViewById(R.id.dateLastPerformedInput);
        timeTextView = (EditText) getView().findViewById(R.id.timeLastPerformedInput);

        categoryTextView = (AutoCompleteTextView) getView().findViewById(R.id.categoryInput);
        repetitionFrequencyTextView = (EditText) getView().findViewById(R.id.repetitionFrequencyInput);
        addPersonButton = (Button) getView().findViewById(R.id.addPersonInteractedButton);
        recordButton = (Button) getView().findViewById(R.id.recordButton);
        nowButton = (Button) getView().findViewById(R.id.nowButton);

        personInteractedListView = (ListView) getView().findViewById(R.id.personInteractedListView);


        person_list = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.person_name_item, person_list);
        personInteractedListView.setAdapter(arrayAdapter);


        _dbHandler = new DBHandler(this.getActivity());
//        _dbHandler.refreshDB();
//        _dbHandler.onCreate();

//        _dbHandler.deleteTables();
//        _dbHandler.createTable();



        // firebase initialization

        // TODO: implement fire base for analytics
//        if(_databaseRef == null) {
//            _firebaseInstance = FirebaseDatabase.getInstance();
//            _firebaseInstance.setPersistenceEnabled(true);
//            _databaseRef = _firebaseInstance.getReference();
//        }


//        DBHandler db = new DBHandler(this, null, null, 1);






        // adjust date and time form sizes

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int width = displayMetrics.widthPixels;
//        dateTextView.setWidth(width/2);
//        timeTextView.setWidth(width/2);

        setupHabitNameAutoComplete();
        setupRepetitionPeriodSpinner();

        setupField(dateTextView);
        setupField(timeTextView);

        setupRepetitionFrequencyField();
        setupAddInteractedPersonButton(addPersonButton);
        setupNowButton();
        setupRecordButton(recordButton);




    }

    private void setupHabitNameAutoComplete() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this.getActivity(),android.R.layout.simple_list_item_1,_dbHandler.getNames(_dbHandler.COL_NAME));
        habitNameTextView.setAdapter(adapter);
        habitNameTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String)parent.getItemAtPosition(position);
                Habit habit = _dbHandler.getHabitByName(selection);
                categoryTextView.setText(habit.getCategory());
            }
        });
    }

    private void setupCategoryAutoComplete() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this.getActivity(),android.R.layout.simple_list_item_1,_dbHandler.getNames(_dbHandler.COL_CATEGORY));
        categoryTextView.setAdapter(adapter);
    }



    private void setupRepetitionPeriodSpinner() {
        repetitionPeriodSpinner = (Spinner) getView().findViewById(R.id.repetitionPeriodSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
                R.layout.hk_spinner_unclicked_textview, periodArray);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.hk_spinner_item);
        // Apply the adapter to the spinner
        repetitionPeriodSpinner.setAdapter(adapter);



    }

    private void setupRepetitionFrequencyField() {

    }




    private void setupRecordButton(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(isNetworkAvailable()) {
                if(areEntriesValid()) {
                    saveToCloud();
                    Toast.makeText(InsertDataFragment.this.getActivity(), R.string.save_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(InsertDataFragment.this.getActivity(), R.string.missing_info, Toast.LENGTH_SHORT).show();
                }
//                } else {


//                }
            }
        });
    }


    private boolean checkLogInStatus(FirebaseUser user) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            return true;
        } else {
            // No user is signed in
            Toast.makeText(InsertDataFragment.this.getActivity(), "User not logged in!", Toast.LENGTH_SHORT);
            return false;
        }
    }




    private Date prepareDatePerformed() {
        Date convertedLastDatePerformed = Utility.stringToDate(dateTextView.getText().toString(), Utility.dateFormat);
        Date convertedLastTimePerformed = Utility.stringToDate(timeTextView.getText().toString(), Utility.timeFormat);
        Calendar d = Calendar.getInstance();
        d.setTime(convertedLastDatePerformed);
        Calendar t = Calendar.getInstance();
        t.setTime(convertedLastTimePerformed);
        Calendar finalCal = Calendar.getInstance();
        finalCal.set(Calendar.DAY_OF_MONTH, d.get(Calendar.DAY_OF_MONTH));
        finalCal.set(Calendar.MONTH, d.get(Calendar.MONTH));
        finalCal.set(Calendar.YEAR, d.get(Calendar.YEAR));
        finalCal.set(Calendar.HOUR, t.get(Calendar.HOUR));
        finalCal.set(Calendar.MINUTE, t.get(Calendar.MINUTE));

        return finalCal.getTime();

    }


    private void setupReminderNotification(Habit habit) {
        Calendar reminderTime = habit.getNextReminderTime();
        if(reminderTime != null) {
            Log.d("Next Reminder Time:", Utility.dateToString(reminderTime.getTime(), Utility.timeFormat));
            Intent intent = new Intent(this.getActivity(), ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getActivity(), 001, intent, 0);
            AlarmManager am = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
        }
    }



    private void saveToCloud() {

        String habitName = habitNameTextView.getText().toString();
        Habit habit = new Habit(prepareDatePerformed(), habitName, "0");
        habit.setCategory(categoryTextView.getText().toString());


        if(!repetitionFrequencyTextView.getText().toString().trim().equals("")) {
            int deltaTime = Integer.parseInt(repetitionFrequencyTextView.getText().toString());
            habit.setReminderTimeAndProperties(repetitionPeriodSpinner.getSelectedItemPosition(), deltaTime);

        }

        // if this habit is already in database, increase the number of times it has been performed.
        if(_dbHandler.habitExist(habitName, habit.getCategory())) {
            _dbHandler.increaseHabitFreq(habitName, habit.getCategory());
        } else {
            _dbHandler.addHabit(habit);
        }

        // laterTODO: interacted person feature
        if(personInteractedListView.getAdapter().getCount() != 0) {
            _dbHandler.addPersonInteracted(person_list, habit.getId());
        }

        setupReminderNotification(habit);

        Log.d(TAG, "Done adding");
        Log.d(TAG, "DB " + _dbHandler.databasetostring());




//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        if(checkLogInStatus(user)) {
//            String uid = user.getUid().toString();
//            Habit habit = new Habit(dateToSave, habitName, uid);
//            _databaseRef.child("habits").setValue(habit);
//            Log.d(TAG, "Data saved");
//            Toast.makeText(InsertDataActivity.this, "Data saved!", Toast.LENGTH_SHORT);
//        } else {
//            Log.d(TAG, "Data not saved");
//            Toast.makeText(InsertDataActivity.this, "No date saved", Toast.LENGTH_SHORT);
//        }

    }
    private boolean areEntriesValid() {
        if(habitNameTextView.getText().toString().trim().equals("")) { return false; }
        if(dateTextView.getText().toString().trim().equals("") || timeTextView.getText().toString().trim().equals("")) { return false; }
        return true;
    }

    private void setupNowButton () {
        nowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateTextView.setText(Utility.dateToString(new Date(), Utility.dateFormat));
                timeTextView.setText(Utility.dateToString(new Date(), Utility.timeFormat));

            }
        });
    }


    public void deleteDatabase() {
        _dbHandler.deleteAllHabits();
    }

    private void setupAddInteractedPersonButton(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(InsertDataFragment.this.getActivity());
                alert.setTitle("Add person");
                alert.setMessage("Enter the name of the person you performed this activity with");

                final EditText input = new EditText(InsertDataFragment.this.getActivity());
                input.setId(R.id.person_name_edit);
                alert.setView(input);

                alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String personName = input.getEditableText().toString();
                        person_list.add(personName);
                        arrayAdapter.notifyDataSetChanged();
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alert.create();
                alertDialog.show();

            }
        });
    }

    private void hideSoftKeyboard(EditText et){
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(InsertDataActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupField(final EditText et) {

        et.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                hideSoftKeyboard(et);
                setDialog(et);
            }
        });

        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideSoftKeyboard(et);
                if (hasFocus) {
                    setDialog(et);
                }
            }
        });

        et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideSoftKeyboard(et);
                return false;
            }
        });
    }



    private void setDialog(EditText et) {
        Calendar now = Calendar.getInstance();
//        Log.d(TAG, "input type:" + et.getInputType());
        if(et.getInputType() == (TYPE_CLASS_DATETIME | TYPE_DATETIME_VARIATION_DATE)) {
            datePickerDialog = new DatePickerDialog(InsertDataFragment.this.getActivity(), new CustomSetListener(et), now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        } else {
            timePickerDialog = new TimePickerDialog(InsertDataFragment.this.getActivity(), new CustomSetListener(et), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        }
    }


}