package com.tytanapps.ptsd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StressTestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StressTestFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String LOG_TAG = StressTestFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StressTestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StressTestFragment newInstance(String param1, String param2) {
        StressTestFragment fragment = new StressTestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public StressTestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_stress_test, container, false);

        LinearLayout questionsLinearLayout = (LinearLayout) rootView.findViewById(R.id.questions_linearlayout);
        insertQuestions(questionsLinearLayout);

        return rootView;
    }

    /**
     * Add the questions to the linear layout
     * @param questionsLinearLayout The linear layout to add the questions to
     */
    private void insertQuestions(LinearLayout questionsLinearLayout) {
        String[] questions = getResources().getStringArray(R.array.stress_questions);

        for(int i = 0; i < questions.length; i++) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.question_box, null, false);

            TextView questionTextView = (TextView) layout.findViewById(R.id.stress_question_textview);
            questionTextView.setText(questions[i]);

            questionsLinearLayout.addView(layout);
        }

        Button submitButton = new Button(getActivity());
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
        int score = getScore();
        if(score < 0)
            Snackbar.make(getView(), "Please answer all of the questions", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        else
            showResults(score);
    }

    /**
     * Display an AlertDialog with the results of the test
     * @param score The score that the user received
     */
    private void showResults(int score) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.stress_test_result, null, false);

        String resultText;
        String nextActions;

        if(score <= 20) {
            resultText = "Your screen results indicate that you have few or no symptoms of PTSD";
            nextActions = "However, you still may wish to consult a professional";
        }

        else if(score <= 29) {
            resultText = "Your screen results are consistent with minimal symptoms of PTSD";
            nextActions = "You may benefit from seeking help from a professional";
        }

        else {
            resultText = "Your screen results are consistent with many of the symptoms of PTSD.";
            nextActions = "You are advised to see your physician or a qualified mental health professional immediately for a complete assessment";
        }

        TextView resultTextView = (TextView) layout.findViewById(R.id.results_textview);
        resultTextView.setText(resultText);

        TextView nextActionsTextview = (TextView) layout.findViewById(R.id.next_steps_textview);
        nextActionsTextview.setText(nextActions);

        final Button shareResultsButton = (Button) layout.findViewById(R.id.share_results_button);
        shareResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareResults();
            }
        });

        final Button findProfessionalButton = (Button) layout.findViewById(R.id.find_professional_button);
        findProfessionalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findProfessional();
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(layout);
        alertDialog.show();
    }

    /**
     * Switch to the NearbyFacilitiesFragment and show a list of nearby facilities
     */
    private void findProfessional() {
        NearbyFacilitiesFragment nearbyFacilitiesFragment = new NearbyFacilitiesFragment();


        android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment
        transaction.replace(R.id.fragment_container, nearbyFacilitiesFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    /**
     * Share the results of the test
     * Creates a share intent. The user can share the results with any app.
     */
    private void shareResults() {
        String shareText = generateShareText();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    /**
     * Generate the text to be shared when completing the test.
     * This includes each question and the answer that was selected.
     * @return A string of the text to be shared
     */
    private String generateShareText() {
        String[] questions = getResources().getStringArray(R.array.stress_questions);
        int[] answers = getEachAnswer();

        String shareText = getString(R.string.stress_prompt);
        shareText += "\n\n";

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
        LinearLayout questionsLinearLayout = (LinearLayout) getView().findViewById(R.id.questions_linearlayout);

        // TODO The number of questions is hard coded
        int score[] = new int[17];
        int questionCount = 0;

        for(int i = 0; i < questionsLinearLayout.getChildCount();i++) {
            View childView = questionsLinearLayout.getChildAt(i);

            if(childView instanceof ViewGroup) {

                SeekBar seekBar = (SeekBar) childView.findViewById(R.id.result_seekbar);
                score[questionCount] = seekBar.getProgress() + 1;

                Log.d(LOG_TAG, seekBar.getProgress() + 1 + "");

                questionCount++;

                /*

                RadioGroup radioGroup = (RadioGroup) childView.findViewById(R.id.questions_radio_group);
                score[questionCount] = radioGroup.getCheckedRadioButtonId();
                questionCount++;

                */
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
        int[] eachAnswer = getEachAnswer();

        for(int num : eachAnswer) {

            if(num > 0)
                score += num;
            else
                return -1;
        }

        return score;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
