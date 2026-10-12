
package com.workout.tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {

    LinearLayout list;
    EditText exercise, sets, reps, weight;
    SharedPreferences prefs;
    JSONArray workouts = new JSONArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("workout_data", MODE_PRIVATE);

        try {
            workouts = new JSONArray(
                prefs.getString("workouts", "[]")
            );
        } catch (Exception ignored) {}

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        root.setBackgroundColor(Color.rgb(245, 247, 250));

        TextView title = new TextView(this);
        title.setText("💪 Workout Tracker");
        title.setTextSize(26);
        title.setTextColor(Color.rgb(25, 35, 50));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Plan your workout. Track your progress.");
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 20);
        root.addView(subtitle);

        exercise = field("Exercise name");
        sets = field("Sets");
        reps = field("Reps");
        weight = field("Weight (kg)");

        root.addView(exercise);
        root.addView(sets);
        root.addView(reps);
        root.addView(weight);

        Button add = new Button(this);
        add.setText("Add Exercise");
        root.addView(add);

        TextView heading = new TextView(this);
        heading.setText("My Exercises");
        heading.setTextSize(21);
        heading.setTextColor(Color.BLACK);
        heading.setPadding(0, 20, 0, 10);
        root.addView(heading);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);

        root.addView(scroll, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        Button clear = new Button(this);
        clear.setText("Clear All Exercises");
        root.addView(clear);

        setContentView(root);

        add.setOnClickListener(v -> addWorkout());
        clear.setOnClickListener(v -> confirmClear());

        render();
    }

    EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setSingleLine(true);
        e.setPadding(16, 8, 16, 8);
        return e;
    }

    void addWorkout() {
        String name = exercise.getText().toString().trim();

        if (name.isEmpty()) {
            exercise.setError("Enter exercise name");
            return;
        }

        try {
            int s = Integer.parseInt(sets.getText().toString());
            int r = Integer.parseInt(reps.getText().toString());
            double w = Double.parseDouble(weight.getText().toString());

            if (s <= 0 || r <= 0 || w < 0) {
                Toast.makeText(this,
                    "Enter valid values", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject item = new JSONObject();
            item.put("name", name);
            item.put("sets", s);
            item.put("reps", r);
            item.put("weight", w);
            item.put("done", false);

            workouts.put(item);
            save();
            exercise.setText("");
            sets.setText("");
            reps.setText("");
            weight.setText("");
            render();

        } catch (Exception e) {
            Toast.makeText(this,
                "Enter sets, reps and weight", Toast.LENGTH_SHORT).show();
        }
    }

    void save() {
        prefs.edit().putString("workouts", workouts.toString()).apply();
    }

    void render() {
        list.removeAllViews();

        for (int i = 0; i < workouts.length(); i++) {
            final int index = i;

            try {
                JSONObject item = workouts.getJSONObject(i);

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(16, 12, 16, 12);
                card.setBackgroundColor(Color.WHITE);

                TextView info = new TextView(this);
                info.setText(item.getString("name")
                    + "\nSets: " + item.getInt("sets")
                    + "   Reps: " + item.getInt("reps")
                    + "   Weight: " + item.getDouble("weight") + " kg");
                info.setTextSize(17);
                info.setTextColor(Color.DKGRAY);
                card.addView(info);

                CheckBox done = new CheckBox(this);
                done.setText("Workout completed");
                done.setChecked(item.getBoolean("done"));
                card.addView(done);

                done.setOnCheckedChangeListener((button, checked) -> {
                    try {
                        workouts.getJSONObject(index).put("done", checked);
                        save();
                    } catch (Exception ignored) {}
                });

                Button delete = new Button(this);
                delete.setText("Delete");
                card.addView(delete);

                delete.setOnClickListener(v -> {
                    workouts.remove(index);
                    save();
                    render();
                });

                LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(-1, -2);
                params.setMargins(0, 0, 0, 12);
                list.addView(card, params);

            } catch (Exception ignored) {}
        }

        if (workouts.length() == 0) {
            TextView empty = new TextView(this);
            empty.setText("No exercises yet. Add your first workout!");
            empty.setTextSize(16);
            list.addView(empty);
        }
    }

    void confirmClear() {
        new AlertDialog.Builder(this)
            .setTitle("Clear exercises?")
            .setMessage("This will delete all saved exercises.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Clear", (d, w) -> {
                workouts = new JSONArray();
                save();
                render();
            })
            .show();
    }
}
