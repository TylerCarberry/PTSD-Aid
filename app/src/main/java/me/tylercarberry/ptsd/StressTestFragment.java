package me.tylercarberry.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


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
     * Calculate the total score and notify the user appropriately
     */
    private void submit() {
        int score = getScore();
        if(score < 0)
            Snackbar.make(getView(), "Please answer all of the questions", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        else
            Toast.makeText(getActivity(), "Score:" + score, Toast.LENGTH_LONG).show();
    }

    /**
     * Determine the total score of the answered questions.
     * If not every question has been answered, return -1
     * Not at all: 1, A little bit: 2, Moderately: 3, Quite a bit: 4, Extremely: 5
     * @return The total score of the questions
     */
    private int getScore() {
        LinearLayout questionsLinearLayout = (LinearLayout) getView().findViewById(R.id.questions_linearlayout);

        int score = 0;

        for(int i = 0; i < questionsLinearLayout.getChildCount(); i++) {
            View childView = questionsLinearLayout.getChildAt(i);

            if(childView instanceof ViewGroup) {

                RadioGroup radioGroup = (RadioGroup) childView.findViewById(R.id.questions_radio_group);

                int radioButtonId = radioGroup.getCheckedRadioButtonId();

                switch (radioButtonId){
                    case R.id.questions_not_at_all:
                        score += 1;
                        break;
                    case R.id.questions_little_bit:
                        score += 2;
                        break;
                    case R.id.questions_moderately:
                        score += 3;
                        break;
                    case R.id.questions_quite_a_bit:
                        score += 4;
                        break;
                    case R.id.questions_extremely:
                        score += 5;
                        break;

                    // A question was not answered
                    default:
                        return -1;
                }
            }
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
