package com.tobiasfried.brewkeeper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TimeUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tobiasfried.brewkeeper.constants.IngredientType;
import com.tobiasfried.brewkeeper.constants.Stage;
import com.tobiasfried.brewkeeper.constants.TeaType;
import com.tobiasfried.brewkeeper.model.Brew;
import com.tobiasfried.brewkeeper.model.Ingredient;
import com.tobiasfried.brewkeeper.utils.TimeUtility;
import com.tobiasfried.brewkeeper.viewmodel.EntryViewModel;
import com.tobiasfried.brewkeeper.viewmodel.EntryViewModelFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;


public class EntryActivity extends AppCompatActivity {

    private static final String LOG_TAG = EntryActivity.class.getSimpleName();
    public static String EXTRA_BREW_ID = "brewID";

    private DateTimeFormatter formatter;

    // Database
    private FirebaseFirestore db;
    private EntryViewModel viewModel;
    private String brewId;
    private DocumentReference docRef;

    // Model
    private Brew currentBrew;
    private List<Ingredient> teas;
    private List<Ingredient> ingredients;
    private List<Ingredient> selectedIngredients;

    // Views
    private EditText brewNameEditText;
    private TextView primaryDateTextView;
    private TextView primaryRemainingDaysTextView;
    private AutoCompleteTextView teaNameEditText;
    private EditText teaAmountEditText;
    private Spinner teaSpinner;
    private Spinner primarySugarSpinner;
    private EditText primarySugarAmountEditText;
    private EditText waterAmountEditText;
    private TextView secondaryDateTextView;
    private TextView secondaryRemainingDaysTextView;
    private Spinner secondarySugarSpinner;
    private EditText secondarySugarAmountEditText;
    private ChipGroup flavorChipGroup;
    private TextView flavorAddTextView;
    private TextView endDateTextView;
    private EditText notesEditText;
    private MaterialButton submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        // Get Date
        formatter = DateTimeFormatter.ofPattern("LLL d");

        // Bind Views
        brewNameEditText = findViewById(R.id.create_edit_text);
        primaryDateTextView = findViewById(R.id.primary_date_calendar);
        primaryRemainingDaysTextView = findViewById(R.id.primary_remaining_days);
        teaNameEditText = findViewById(R.id.tea_name_autocomplete);
        teaAmountEditText = findViewById(R.id.tea_amount_picker);
        teaSpinner = findViewById(R.id.tea_picker);
        primarySugarSpinner = findViewById(R.id.primary_sugar_picker);
        primarySugarAmountEditText = findViewById(R.id.sugar_amount_picker);
        waterAmountEditText = findViewById(R.id.water_amount_picker);
        secondaryDateTextView = findViewById(R.id.secondary_date_calendar);
        secondaryRemainingDaysTextView = findViewById(R.id.secondary_remaining_days);
        secondarySugarSpinner = findViewById(R.id.secondary_sugar_picker);
        secondarySugarAmountEditText = findViewById(R.id.secondary_sugar_amount_picker);
        flavorChipGroup = findViewById(R.id.ingredient_chip_group);
        flavorAddTextView = findViewById(R.id.add_ingredient_edit_text);
        endDateTextView = findViewById(R.id.end_date_calendar);
        notesEditText = findViewById(R.id.notes);
        submitButton = findViewById(R.id.button_start);

        // TODO fetch from loaded brew
        selectedIngredients = new ArrayList<>();

        // Get Database instance
        db = FirebaseFirestore.getInstance();

        // Get Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.menu_entry);



        brewId = getIntent().hasExtra(EXTRA_BREW_ID) ? Objects.requireNonNull(getIntent().getExtras()).getString(EXTRA_BREW_ID) : null;
        EntryViewModelFactory factory = new EntryViewModelFactory(db, brewId);
        viewModel = ViewModelProviders.of(this, factory).get(EntryViewModel.class);
        fetchBrew();

        docRef = viewModel.getDocumentReference();
        Log.i(LOG_TAG, docRef.toString());

        setupSpinners();
        setupDialogs();
        setupEntryFields();
        setupButtons();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                finish();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get ingredients from database
        fetchIngredients();
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> teaTypes = ArrayAdapter.createFromResource(this, R.array.array_tea_types, R.layout.spinner_item_small);
        teaTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teaSpinner.setAdapter(teaTypes);
        teaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO set values on current brew (also in other pickers)
                //currentTeas.getRecipe().getTeas().get(0).setTeaType(TeaType.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> sweetenerTypes = ArrayAdapter.createFromResource(this, R.array.array_sugar_types, R.layout.spinner_item);
        sweetenerTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        primarySugarSpinner.setAdapter(sweetenerTypes);
        secondarySugarSpinner.setAdapter(sweetenerTypes);
    }

    private void setupEntryFields() {
        TextWatcher requiredWatcher = new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = brewNameEditText.getText().toString().trim();
                String teaName = teaNameEditText.getText().toString().trim();
                String teaAmount = teaAmountEditText.getText().toString().trim();
                String primarySugarAmount = primarySugarAmountEditText.getText().toString().trim();
                String waterAmount = waterAmountEditText.getText().toString().trim();

                submitButton.setEnabled(!name.isEmpty() && !teaName.isEmpty() && !teaAmount.isEmpty() &&
                        !primarySugarAmount.isEmpty() && !waterAmount.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        brewNameEditText.addTextChangedListener(requiredWatcher);
        teaNameEditText.addTextChangedListener(requiredWatcher);
        teaAmountEditText.addTextChangedListener(requiredWatcher);
        primarySugarAmountEditText.addTextChangedListener(requiredWatcher);
        waterAmountEditText.addTextChangedListener(requiredWatcher);

    }

    private void setupDialogs() {
        primaryDateTextView.setText(formatter.format(LocalDateTime.now()));
        primaryDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateSelectionDialog fragment = DateSelectionDialog.getInstance();
                fragment.setDate(currentBrew.getPrimaryStartDate());
                fragment.setMaxDate(currentBrew.getSecondaryStartDate());
                fragment.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        currentBrew.setPrimaryStartDate(new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis());
                        refreshDates();
                    }
                });
                fragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        secondaryDateTextView.setText(formatter.format(LocalDateTime.now().plusDays(10)));
        secondaryDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateSelectionDialog fragment = DateSelectionDialog.getInstance();
                fragment.setMinDate(currentBrew.getPrimaryStartDate());
                fragment.setDate(currentBrew.getSecondaryStartDate());
                fragment.setMaxDate(currentBrew.getEndDate());
                fragment.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        currentBrew.setSecondaryStartDate(new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis());
                        refreshDates();
                    }
                });
                fragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        endDateTextView.setText(formatter.format(LocalDateTime.now().plusDays(12)));
        endDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateSelectionDialog fragment = DateSelectionDialog.getInstance();
                fragment.setMinDate(currentBrew.getSecondaryStartDate());
                fragment.setDate(currentBrew.getEndDate());
                fragment.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        currentBrew.setEndDate(new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis());
                        refreshDates();
                    }
                });
                fragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    private void setupButtons() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBrew();
                finish();
            }
        });
    }

    private void fetchBrew() {
        viewModel.getBrew().observe(this, new Observer<Brew>() {
            @Override
            public void onChanged(Brew brew) {
                currentBrew = brew;
                if (brewId != null) {
                    setupBrew();
                } else {
                    refreshDates();
                }
            }
        });
    }

    private void fetchIngredients() {
        viewModel.getFlavors().observe(this, new Observer<List<Ingredient>>() {
            @Override
            public void onChanged(List<Ingredient> allIngredients) {
                ingredients = allIngredients;
                setupChips();
            }
        });

        viewModel.getTeas().observe(this, new Observer<List<Ingredient>>() {
            @Override
            public void onChanged(List<Ingredient> allTeas) {
                teas = allTeas;
            }
        });
    }

    private void setupBrew() {
        brewNameEditText.setText(currentBrew.getRecipe().getName());
        refreshDates();
        teaNameEditText.setText(currentBrew.getRecipe().getTeas().get(0).getName(), TextView.BufferType.EDITABLE);
        teaAmountEditText.setText(String.valueOf(currentBrew.getRecipe().getTeas().get(0).getAmount()), TextView.BufferType.EDITABLE);
        teaSpinner.setSelection(currentBrew.getRecipe().getTeas().get(0).getTeaType().getCode());
        primarySugarSpinner.setSelection(currentBrew.getRecipe().getPrimarySweetener());
        primarySugarAmountEditText.setText(String.valueOf(currentBrew.getRecipe().getPrimarySweetenerAmount()), TextView.BufferType.EDITABLE);
        waterAmountEditText.setText(String.valueOf(currentBrew.getRecipe().getWater()));
        secondarySugarSpinner.setSelection(currentBrew.getRecipe().getSecondarySweetener());
        secondarySugarAmountEditText.setText(String.valueOf(currentBrew.getRecipe().getSecondarySweetenerAmount()), TextView.BufferType.EDITABLE);
        selectedIngredients.addAll(currentBrew.getRecipe().getIngredients());
        notesEditText.setText(currentBrew.getRecipe().getNotes());

    }

    private void setupChips() {
        flavorChipGroup.removeAllViews();
        for (final Ingredient i : ingredients) {
            final Chip chip = new Chip(this, null, R.style.ChipTheme);
            chip.setText(i.getName().toLowerCase());
            chip.setTextColor(getColorStateList(R.color.color_states_chips));
            chip.setTypeface(getResources().getFont(R.font.google_sans_medium));
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipStrokeColorResource(R.color.color_states_chips);
            chip.setChipStrokeWidth(4.0f);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            if (currentBrew != null && currentBrew.getRecipe().getIngredients().contains(i)) {
                chip.setChecked(true);
            }
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedIngredients.add(i);
                    } else {
                        selectedIngredients.remove(i);
                    }
                }
            });
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentBrew.getRecipe().getIngredients().remove(i);
                    db.collection(Ingredient.COLLECTION).whereEqualTo("name", i.getName()).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            db.collection(Ingredient.COLLECTION).document(document.getId()).delete();
                                        }
                                    }
                                }
                            });
                }
            });
            chip.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (chip.isCloseIconVisible()) {
                        chip.setCheckable(true);
                    } else {
                        chip.setChecked(false);
                        chip.setCheckable(false);
                    }
                    chip.setCloseIconVisible(!chip.isCloseIconVisible());
                    return true;
                }
            });
            flavorChipGroup.addView(chip);
        }

        flavorAddTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputDialog newFragment = InputDialog.getInstance();
                newFragment.setOnClickListener(new InputDialog.InputSubmitListener() {
                    @Override
                    public void onSubmitInput(DialogFragment dialog, final String input) {
                        if (!input.equals("")) {
                            Ingredient newIngredient = new Ingredient(input, IngredientType.FLAVOR, null, 0);
                            db.collection(Ingredient.COLLECTION).add(newIngredient);
                            setupChips();
                        }
                    }
                });

                newFragment.show(getSupportFragmentManager(), "ingredientInput");
            }
        });
    }

    private void refreshDates() {
        primaryDateTextView.setText(formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(currentBrew.getPrimaryStartDate()),
                ZoneId.systemDefault())));
        int primaryDays = TimeUtility.daysBetween(currentBrew.getPrimaryStartDate(), currentBrew.getSecondaryStartDate());
        String primaryString = getResources().getQuantityString(R.plurals.pluralDays, primaryDays, primaryDays);
        primaryRemainingDaysTextView.setText(primaryString);
        secondaryDateTextView.setText(formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(currentBrew.getSecondaryStartDate()),
                ZoneId.systemDefault())));
        int secondaryDays = TimeUtility.daysBetween(currentBrew.getSecondaryStartDate(), currentBrew.getEndDate());
        String secondaryString = getResources().getQuantityString(R.plurals.pluralDays, secondaryDays, secondaryDays);
        secondaryRemainingDaysTextView.setText(secondaryString);
        endDateTextView.setText(formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(currentBrew.getEndDate()),
                ZoneId.systemDefault())));
    }

    private void saveBrew() {

        // Read Fields
        currentBrew.getRecipe().setName(brewNameEditText.getText().toString().trim());
        currentBrew.getRecipe().setPrimarySweetenerAmount(Integer.parseInt(primarySugarAmountEditText.getText().toString()));
        currentBrew.getRecipe().setWater(Double.parseDouble(waterAmountEditText.getText().toString()));
        currentBrew.getRecipe().setSecondarySweetenerAmount(Integer.parseInt(secondarySugarAmountEditText.getText().toString()));
        currentBrew.getRecipe().setIngredients(selectedIngredients);
        currentBrew.getRecipe().setNotes(notesEditText.getText().toString().trim());

        Ingredient tea = new Ingredient();
        tea.setName(teaNameEditText.getText().toString().trim());
        tea.setType(IngredientType.TEA);
        tea.setTeaType(TeaType.get(teaSpinner.getSelectedItemPosition()));
        tea.setAmount(Integer.parseInt(teaAmountEditText.getText().toString()));
        currentBrew.getRecipe().addTea(tea);

        if (Instant.now().isAfter(Instant.ofEpochMilli(currentBrew.getPrimaryStartDate())) &&
                Instant.now().isBefore(Instant.ofEpochMilli(currentBrew.getSecondaryStartDate()))) {
            currentBrew.setStage(Stage.PRIMARY);
            currentBrew.setRunning(true);
        } else if (Instant.now().isAfter(Instant.ofEpochMilli(currentBrew.getSecondaryStartDate())) &&
                Instant.now().isBefore(Instant.ofEpochMilli(currentBrew.getEndDate()))) {
            currentBrew.setStage(Stage.SECONDARY);
            currentBrew.setRunning(true);
        } else {
            currentBrew.setRunning(false);
            currentBrew.setStage(null);
        }

        docRef.set(currentBrew).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    finish();
//                } else {
//                    Snackbar.make(XX, "Error making database changes", Snackbar.LENGTH_INDEFINITE)
//                            .setAction("Retry", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    saveBrew();
//                                }
//                            })
//                            .show();
                }
            }
        });


    }
}
