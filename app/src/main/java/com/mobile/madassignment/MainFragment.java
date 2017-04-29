package com.mobile.madassignment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.madassignment.Adapter.ExpenseViewHolder;
import com.mobile.madassignment.Adapter.GroupMemberViewAdapter;
import com.mobile.madassignment.Adapter.GroupMemberViewHolder;
import com.mobile.madassignment.models.Expense;
import com.mobile.madassignment.models.GroupMember;
import com.mobile.madassignment.util.DataFormat;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private static final int REQUEST_INVITE = 0;

    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView myExpenseRecyclerView;
    private ListView main_lv;
    private FloatingActionButton add_expense;
    private String group_key;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference groupRef;
    private FirebaseRecyclerAdapter<Expense, ExpenseViewHolder> mFirebaseAdapter;
    private RelativeLayout rv_balance;
    private TextView tv_balence_value;
    private TextView tv_owenORowendText;
    private TextView tv_mySpending;
    private TextView tv_totalSpending;
    private RecyclerView rv_members;
    private LinearLayout ll_invite_button;

    private List<Long> expenseKeys;
    private int numOfmembers=1;
    private float totalSpending;
    private GroupMember me;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Map<String, String> suerId_nameMap;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Bundle bundle = this.getArguments();
        if(bundle!= null){
            group_key = bundle.getString("group_key");
            Log.v("get_arg ", group_key);
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_main, container, false);

        add_expense = (FloatingActionButton) v.findViewById(R.id.bt_add_expense);
        myExpenseRecyclerView = (RecyclerView) v.findViewById(R.id.expenseRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this.getActivity());
        rv_members = (RecyclerView)v.findViewById(R.id.rv_members);
        rv_balance = (RelativeLayout)v.findViewById(R.id.rv_balance);
        ll_invite_button = (LinearLayout)v.findViewById(R.id.add_user_to_group);
        tv_balence_value = (TextView)v.findViewById(R.id.tv_my_balance);
        tv_owenORowendText= (TextView)v.findViewById(R.id.tv_own_or_owned);
        tv_mySpending= (TextView)v.findViewById(R.id.tv_my_spending);
        tv_totalSpending= (TextView)v.findViewById(R.id.tv_group_spending);
        expenseKeys = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        me = new GroupMember();

        mLinearLayoutManager.setStackFromEnd(false);
        myExpenseRecyclerView.setLayoutManager(mLinearLayoutManager);

        mDatabaseRef  = FirebaseDatabase.getInstance().getReference();
        suerId_nameMap = new HashMap<>();
        mDatabaseRef.child("groups").child(group_key).child("members").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                suerId_nameMap.put(dataSnapshot.getKey(),dataSnapshot.getValue().toString());
                Log.d("put to map", dataSnapshot.getKey()+"= "+dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Expense, ExpenseViewHolder>(
                Expense.class,
                R.layout.expense_item,
                ExpenseViewHolder.class,
                mDatabaseRef.child("expenses").child(group_key)) {
            @Override
            protected void populateViewHolder(ExpenseViewHolder viewHolder, Expense expense, int position) {
                Log.v("expense.type",expense.getType());
                Log.v("expense.cost",expense.getCost()+"");

                String type = expense.getType();
                if(expense.getDescription().matches("")||expense.getDescription()==null){
                    viewHolder.type.setText(type);

                }else {
                    viewHolder.type.setText(expense.getDescription());
                }
                viewHolder.cost.setText(expense.getCost() +" €");
                if(expense.getPayer().matches(user.getUid())){  //TODO
                    viewHolder.payer.setText("you payed ");
                    viewHolder.payer.setTextColor(Color.GREEN);
                }else {

                    viewHolder.payer.setText( suerId_nameMap.get(expense.getPayer())+ " payed ");
                    viewHolder.payer.setTextColor(Color.RED);
                }
                switch (type){
                    case "home":
                        viewHolder.typeImgView.setImageResource(R.mipmap.ic_home);
                        break;
                    case "trip":
                        viewHolder.typeImgView.setImageResource(R.mipmap.ic_bus);
                        break;
                    case "shopping" :
                        viewHolder.typeImgView.setImageResource(R.mipmap.ic_handcart);
                        break;
                    case "food":
                        viewHolder.typeImgView.setImageResource(R.mipmap.ic_bowl);
                        break;
                    case "other":
                        viewHolder.typeImgView.setImageResource(R.mipmap.ic_other);
                        break;
                }

                //TODO get userid (userid1 is a default id)
                if(!expenseKeys.contains(expense.getCreateTime())){
                    if (expense.getPayer().matches(user.getUid())) {
                        float temp = me.getPayed() + expense.getCost();
                        me.setPayed(temp);
                    }
                    totalSpending += expense.getCost();
                    Log.v("payerId", expense.getPayer());
                    Log.v("me_payed", me.getPayed() + "");
                    Log.v("me_spending", me.getSpending() + "");
                    Log.v("totalSpending ", totalSpending + "");

                    float mySpending = totalSpending / numOfmembers;
                    Log.v("numOfmembers ", numOfmembers + "");
                    Log.v("mySpending ", mySpending + "");
                    float mybalance = me.getPayed() - mySpending;
                    tv_balence_value.setText(DataFormat.myDFloatFormat(mybalance));
                    if (mybalance < 0) {
                        Log.v("Mybalance",mybalance+"");
                        tv_owenORowendText.setText("you owen");
                        tv_owenORowendText.setBackgroundColor(Color.RED);
                    }

                    tv_mySpending.setText(DataFormat.myDFloatFormat(mySpending));
                    tv_totalSpending.setText(DataFormat.myDFloatFormat(totalSpending));
                    expenseKeys.add(expense.getCreateTime());
                }

            }
        };


        myExpenseRecyclerView.setLayoutManager(mLinearLayoutManager);
        myExpenseRecyclerView.setAdapter(mFirebaseAdapter);

        DatabaseReference groupMembersRef = mDatabaseRef.child("groups").child(group_key).child("members");

        DatabaseReference userRef = mDatabaseRef.child("users");
        //get group members
        List<String> names = new ArrayList<>();
        final GroupMemberViewAdapter groupAdapter = new GroupMemberViewAdapter(names , this.getContext());

        groupMembersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                numOfmembers = (int)dataSnapshot.getChildrenCount();
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    String userId = data.getKey();
                    String userName = data.getValue().toString();
                    Log.v("test key",userId);
                    groupAdapter.getNames().add(userName);
                }
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getActivity(),LinearLayoutManager.HORIZONTAL,false);
        
       // groupAdapter.getNames().add("add_new_member");

        rv_members.setLayoutManager(layoutManager);
        rv_members.setAdapter(groupAdapter);
////////////
        add_expense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString("group_key",group_key);
                Intent intent = new Intent(getActivity(),AddNewExpenseActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        rv_balance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),BalanceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("group_key",group_key);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        ll_invite_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                        .setMessage(getString(R.string.invitation_message))
                        .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)+"$"+group_key))
                        .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                        .setCallToActionText(getString(R.string.invitation_cta))

                        .build();
                startActivityForResult(intent,REQUEST_INVITE);
            }
        });


        return v;



    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == android.app.Activity.RESULT_OK) {
               // DatabaseReference invitations = mDatabaseRef.child("invitations");
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);

                for (String id : ids) {

                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // [START_EXCLUDE]
                showMessage(getString(R.string.send_failed));
                // [END_EXCLUDE]
            }
        }
    }
    // [END on_activity_result]

    private void showMessage(String msg) {
        ViewGroup container = (ViewGroup) getActivity().findViewById(R.id.snackbar_layout);
        Snackbar.make(container, msg, Snackbar.LENGTH_SHORT).show();
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
