package com.tytanapps.ptsd.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tytanapps.ptsd.MainActivity;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.facility.FacilitiesFragment;
import com.tytanapps.ptsd.firebase.RemoteConfig;
import com.tytanapps.ptsd.utils.ExternalAppUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.techery.progresshint.ProgressHintDelegate;

import static butterknife.ButterKnife.findById;


/**
 * A short multiple choice quiz to determine if you suffer from PTSD. Based on the results, gives
 * you recommendations on what to do next. Find a professional is always a recommendation even if
 * the user shows no signs of PTSD.
 */
public class PTSDTestFragment extends BaseFragment {

    @BindView(R.id.questions_linearlayout) LinearLayout questionsLinearLayout;


    public PTSDTestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_ptsd_test, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setupQuestionsLayout();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setCheckedNavigationItem(R.id.nav_test);
    }

    @Override
    protected @StringRes int getTitle() {
        return R.string.ptsd_test_title;
    }

    /**
     * Add the prompt and the questions to the layout
     */
    private void setupQuestionsLayout() {
        if(RemoteConfig.getBoolean(getActivity(), R.string.rc_questions_sticky)) {
            TextView headerTextView = findById(questionsLinearLayout, R.id.stress_textview);
            headerTextView.setTag("sticky");
        }

        insertQuestions(questionsLinearLayout);
    }

    /**
     * Add the questions to the linear layout
     * @param questionsLinearLayout The linear layout to add the questions to
     */
    private void insertQuestions(LinearLayout questionsLinearLayout) {
        String[] questions = getResources().getStringArray(R.array.stress_questions);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for(String question : questions) {
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.question_box, getRootViewGroup(), false);

            TextView questionTextView = findById(layout, R.id.stress_question_textview);
            questionTextView.setText(question);

            questionsLinearLayout.addView(layout);

            io.techery.progresshint.addition.widget.SeekBar seekBar =
                    findById(layout, R.id.result_seekbar);

            seekBar.getHintDelegate()
                    .setHintAdapter(new ProgressHintDelegate.SeekBarHintAdapter() {
                        @Override public String getHint(android.widget.SeekBar seekBar, int progress) {
                            String progressHint;
                            if(progress < seekBar.getMax() / 5.0)
                                progressHint = getString(R.string.not_at_all);
                            else if(progress < seekBar.getMax() * 2.0/5)
                                progressHint = getString(R.string.little_bit);
                            else if(progress < seekBar.getMax() * 3.0/5)
                                progressHint = getString(R.string.moderately);
                            else if(progress < seekBar.getMax() * 4.0/5)
                                progressHint = getString(R.string.quite_a_bit);
                            else
                                progressHint = getString(R.string.extremely);
                            
                            return progressHint;
                        }
                    });
        }

        addSubmitButton(questionsLinearLayout);
    }

    /**
     * Add the submit button after the list of questions
     * @param questionsLinearLayout The layout to add the submit button to
     */
    private void addSubmitButton(LinearLayout questionsLinearLayout) {
        Button submitButton = new Button(getActivity());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        int horizontalMargin = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
        int verticalMargin = (int) getResources().getDimension(R.dimen.activity_vertical_margin);
        params.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
        submitButton.setLayoutParams(params);

        // Set the appearance of the button
        submitButton.setPadding(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
        submitButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        submitButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        submitButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        submitButton.setText(getString(R.string.submit_test));

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        questionsLinearLayout.addView(submitButton);
    }

    /**
     * The user has pressed the submit button.
     * Calculate the total score and notify the user appropriately
     */
    private void submit() {
        sendAnalyticsEvent("Action", "Submit Test");
        int score = getScore();
        showResults(score);
    }

    /**
     * Display an AlertDialog with the results of the test
     * @param score The score that the user received
     */
    private void showResults(int score) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setPositiveButton(R.string.find_professional, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                findProfessional();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.share_results, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                shareResults();
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.ptsd_result_dialog, getRootViewGroup(), false);

        String resultText;
        String nextActions;

        // Low
        if(score <= 20) {
            resultText = getString(R.string.result_minimal);
            nextActions = getString(R.string.see_professional_low);
        }
        // Medium
        else if(score <= 29) {
            resultText = getString(R.string.result_medium);
            nextActions = getString(R.string.see_professional_medium);
        }
        // High
        else {
            resultText = getString(R.string.result_high);
            nextActions = getString(R.string.see_professional_high);
        }

        TextView resultTextView = findById(layout, R.id.results_textview);
        resultTextView.setText(resultText);

        TextView nextActionsTextView = findById(layout, R.id.next_steps_textview);
        nextActionsTextView.setText(nextActions);

        alertDialog.setView(layout);
        alertDialog.show();
    }

    /**
     * Switch to the FacilitiesFragment and show a list of nearby facilities
     */
    private void findProfessional() {
        ((MainActivity)getActivity()).switchFragment(new FacilitiesFragment());
    }

    /**
     * Share the results of the test
     * Creates a share intent. The user can share the results with any app.
     */
    private void shareResults() {
        ExternalAppUtil.shareTextIntent(getActivity(), getShareText());
    }

    /**
     * Generate the text to be shared when completing the test.
     * This includes each question and the answer that was selected.
     * @return A string of the text to be shared
     */
    private String getShareText() {
        String[] questions = getResources().getStringArray(R.array.stress_questions);
        int[] answers = getEachAnswer();

        String shareText = getString(R.string.stress_prompt) + "\n\n";

        for(int i = 0; i < questions.length; i++) {
            // Include each question prompt
            shareText += (i+1) + ") ";
            shareText += questions[i] + "\n";
            shareText += "-";

            switch (answers[i]) {
                case 1:
                    shareText += getString(R.string.not_at_all);
                    break;
                case 2:
                    shareText += getString(R.string.little_bit);
                    break;
                case 3:
                    shareText += getString(R.string.moderately);
                    break;
                case 4:
                    shareText += getString(R.string.quite_a_bit);
                    break;
                case 5:
                    shareText += getString(R.string.extremely);
                    break;
                default:
                    shareText += "N/A";
                    break;
            }

            // Skip a line between each question
            shareText += "\n\n";
        }

        return shareText;
    }

    /**
     * Get each answer that the user has selected.
     * Each element of the array has the id of the radio button that was selected for that question,
     * -1 if no answer was provided for that question
     * 1-5
     * @return An array of the answers
     */
    private int[] getEachAnswer() {
        // TODO The number of questions is hard coded
        int score[] = new int[17];
        int questionCount = 0;

        View rootView = getView();
        if(rootView != null) {
            for (int i = 0; i < questionsLinearLayout.getChildCount(); i++) {
                View childView = questionsLinearLayout.getChildAt(i);

                if (childView instanceof ViewGroup) {
                    SeekBar seekBar = findById(childView, R.id.result_seekbar);
                    score[questionCount] = seekBar.getProgress()/((seekBar.getMax()+1)/5) + 1;

                    questionCount++;
                }
            }
        }
        return score;
    }

    /**
     * Determine the total score of the answered questions.
     * If not every question has been answered, return -1
     * Not at all: 1, A little bit: 2, Moderately: 3, Quite a bit: 4, Extremely: 5
     * @return The total score of the questions
     */
    private int getScore() {
        int score = 0;
        
        for(int num : getEachAnswer()) {
            if(num > 0)
                score += num;
            // If a question has not been answered
            else
                return -1;
        }

        return score;
    }

}
