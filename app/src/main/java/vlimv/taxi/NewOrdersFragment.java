package vlimv.taxi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 17-Mar-18.
 */

public class NewOrdersFragment extends Fragment {
    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 15;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    private OnListItemClickListener mListItemClickListener;
    protected RecyclerView mRecyclerView;
    protected static NewOrdersAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected static List<JSONObject> trips = new ArrayList<>();

    private static Activity activity;

    private Socket mSocket;

    private TextView emptyTV;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.
        //initDataset();
        activity = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page, container, false);
        rootView.setTag(TAG);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");

        emptyTV = rootView.findViewById(R.id.empty_text_view);

        mRecyclerView = rootView.findViewById(R.id.main_recycler);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        mAdapter = new NewOrdersAdapter(trips);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemTapListener(mListItemClickListener);
        setRecyclerViewLayoutManager(LayoutManagerType.LINEAR_LAYOUT_MANAGER);

        TaxiApplication app = (TaxiApplication) getActivity().getApplication();
        mSocket = app.getSocket();
        mSocket.on("active_trips", onActiveTrips);
        mSocket.connect();
        return rootView;
    }
    private Emitter.Listener onActiveTrips = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            for (Object arg: args) {
                Log.d("onActiveTrips", arg.toString());
            }
            try {
                JSONArray arr = new JSONArray(args[0].toString());
                final ArrayList<JSONObject> list = new ArrayList<>();
                if (list.size() == 0)
                    emptyTV.setVisibility(View.VISIBLE);
                for (int i = 0; i < arr.length(); i++) {
                    Log.d("onActiveTripsList", arr.getJSONObject(i).toString());
                    list.add(arr.getJSONObject(i));
                }
                // TODO pass active trips to trips view
//                OrdersFragment.initDataset(list);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("OrdersFragment", "request update list from ui thread");
                        updateActiveTrips(list);
                    }
                });
            } catch (JSONException e) {
                Log.e("onActiveTripsListener", e.getMessage());
            }
        }
    };
    public void updateActiveTrips(ArrayList<JSONObject> list) {
        // trips = list;
        trips.clear();
        trips.addAll(list);
        mAdapter.notifyDataSetChanged();
        Log.d("OrdersFragment", "Notified adapter");
    }

    @Override
    public void onDestroy() {
        mSocket.off("active_trips", onActiveTrips);
        mSocket.disconnect();
        super.onDestroy();
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    public static void initDataset(ArrayList<JSONObject> array) {
        trips = array;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });

        Log.d("TRIPS", "trips in initdataset");
        for (int i = 0; i < trips.size(); i++) {
            Log.d("TRIPS", trips.get(i).toString());
            //trips.add("This is element #" + i);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListItemClickListener = new OnListItemClickListener() {
            @Override
            public void onListItemClick(JSONObject trip) {
                    Dialog_details dialog = new Dialog_details(getActivity());
                    dialog.showDialog(getActivity());
            }
        };

//        try {
//            mListItemClickListener = (OnListItemClickListener) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString()
//                    + " must implement OnListItemClickListener");
//        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        //mListItemClickListener = null;
    }

    public interface OnListItemClickListener {
        void onListItemClick(JSONObject trip);
    }
    void startOrder() {
        Intent intent = new Intent(getContext(), DriverOrderActivity.class);
        startActivity(intent);

    }

    public class Dialog_details extends android.app.Dialog {
        public Dialog_details(Activity a) {
            super(a);
        }

        public void showDialog(Activity activity){
            final Dialog_details dialog = new Dialog_details(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_details);

            TextView text_cancel = dialog.findViewById(R.id.text_cancel);
            TextView text_accept = dialog.findViewById(R.id.text_accept);
            text_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Заказ не беру.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            text_accept.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Заказ принят.", Toast.LENGTH_SHORT).show();
                    startOrder();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }
}
