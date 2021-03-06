package com.raizlabs.universaladapter.converter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.raizlabs.universaladapter.RecyclerViewItemClickListener;
import com.raizlabs.universaladapter.RecyclerViewListObserverListener;
import com.raizlabs.universaladapter.ViewHolder;

/**
 * Class which dynamically converts a {@link UniversalAdapter} into a
 * {@link RecyclerView.Adapter}. This keeps a binding to the
 * {@link UniversalAdapter} so it will be notified of data changes made to the
 * outer adapter.
 *
 * @param <Item>   The type of item that views will represent.
 * @param <Holder> The type of the {@link ViewHolder} that will be used to hold
 *                 views.
 */
public class RecyclerViewAdapterConverter<Item, Holder extends ViewHolder>
        extends RecyclerView.Adapter implements UniversalConverter<Item, Holder> {

    // region Interface Declarations

    /**
     * Provides more specific information for a click, separate from {@link ItemClickedListener}
     */
    public interface RecyclerItemClickListener<Holder extends ViewHolder> {

        /**
         * Called when an item in the {@link RecyclerView} is clicked.
         *
         * @param viewHolder The view holder of the clicked item.
         * @param parent     The recycler view which contained the clicked item.
         * @param position   The position in the adapter of the clicked item.
         */
        void onItemClick(Holder viewHolder, RecyclerView parent, int position, float x, float y);
    }

    // endregion Interface Declaration

    // region Members

    private UniversalAdapter<Item, Holder> universalAdapter;
    private RecyclerItemClickListener<Holder> recyclerItemClickListener;
    private RecyclerViewListObserverListener<Item> observerListener;

    // endregion Members

    /**
     * Creates a {@link RecyclerViewAdapterConverter} converting the given {@link UniversalAdapter} into a
     * {@link android.support.v7.widget.RecyclerView.Adapter}.
     *
     * @param universalAdapter The adapter to convert.
     * @param recyclerView     The {@link RecyclerView} to bind to. This may be null, but it is heavily recommended
     *                         that it isn't. If null is passed, you'll likely want to call
     *                         {@link #bindToRecyclerView(RecyclerView)} - see its documentation for more details.
     */
    public RecyclerViewAdapterConverter(@NonNull
                                        UniversalAdapter<Item, Holder> universalAdapter, RecyclerView recyclerView) {
        observerListener = new RecyclerViewListObserverListener<>(this);
        universalAdapter.checkIfBoundAndSet();
        setAdapter(universalAdapter);
        bindToRecyclerView(recyclerView);
        universalAdapter.notifyDataSetChanged();
    }

    // region Instance Methods

    /**
     * Sets the listener to be called when an item is clicked. This call back provides more
     * information about the click event of the {@link RecyclerView}
     *
     * @param recyclerItemClickListener The listener to call.
     */
    public void setRecyclerItemClickListener(RecyclerItemClickListener<Holder> recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }

    // endregion Instance Methods

    // region Inherited Methods

    @Override
    public UniversalAdapter<Item, Holder> getAdapter() {
        return universalAdapter;
    }

    @Override
    public void setItemClickedListener(ItemClickedListener<Item, Holder> listener) {
        getAdapter().setItemClickedListener(listener);
    }

    @Override
    public void setItemLongClickedListener(ItemLongClickedListener<Item, Holder> longClickedListener) {
        getAdapter().setItemLongClickedListener(longClickedListener);
    }

    @Override
    public void setHeaderClickedListener(HeaderClickedListener headerClickedListener) {
        getAdapter().setHeaderClickedListener(headerClickedListener);
    }

    @Override
    public void setFooterClickedListener(FooterClickedListener footerClickedListener) {
        getAdapter().setFooterClickedListener(footerClickedListener);
    }

    @Override
    public void setHeaderLongClickedListener(HeaderLongClickedListener headerLongClickedListener) {
        getAdapter().setHeaderLongClickedListener(headerLongClickedListener);
    }

    @Override
    public void setFooterLongClickedListener(FooterLongClickedListener footerLongClickedListener) {
        getAdapter().setFooterLongClickedListener(footerLongClickedListener);
    }

    @Override
    public void cleanup() {
        getAdapter().getListObserver().removeListener(observerListener);
    }

    @Override
    public void setAdapter(@NonNull
                           UniversalAdapter<Item, Holder> listAdapter) {
        if (getAdapter() != null) {
            getAdapter().getListObserver().removeListener(observerListener);
        }

        this.universalAdapter = listAdapter;
        // Add a listener which will delegate list observer calls back to us
        listAdapter.getListObserver().addListener(observerListener);
        setHasStableIds(listAdapter.hasStableIds());
    }

    @Override
    public long getItemId(int position) {
        return getAdapter().getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return getAdapter().getInternalItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return getAdapter().getInternalCount();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        getAdapter().bindViewHolder((ViewHolder) viewHolder, position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return getAdapter().createViewHolder(parent, viewType);
    }

    // endregion Inherited Methods

    // region Instance Methods

    /**
     * Binds this adapter to the given {@link RecyclerView}, setting it as its adapter. This should be done by
     * construction or immediately after, before this adapter is used. This mechanism sets this class as the view's
     * adapter and permits certain functionality such as click events. Without it, this class will still function as
     * a normal {@link android.support.v7.widget.RecyclerView.Adapter}, but additional functionality may not work.
     * Ignore this step at your own risk.
     *
     * @param recyclerView The {@link RecyclerView} to bind to.
     */
    public void bindToRecyclerView(RecyclerView recyclerView) {
        if (recyclerView != null) {
            recyclerView.setAdapter(this);
            recyclerView.addOnItemTouchListener(internalOnItemTouchListener);
        }
    }

    // region Anonymous Classes

    private final RecyclerViewItemClickListener internalOnItemTouchListener = new RecyclerViewItemClickListener() {
        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            //TODO
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onItemClick(ViewHolder viewHolder, RecyclerView parent, int position, float x, float y) {
            if (getAdapter().internalIsEnabled(position)) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onItemClick((Holder) viewHolder, parent, position, x, y);
                }

                getAdapter().onItemClicked(position, viewHolder);
            }
        }

        @Override
        public void onItemLongClick(ViewHolder viewHolder, RecyclerView parent, int position, float x, float y) {
            getAdapter().onItemLongClicked(position, viewHolder);
        }
    };

    // endregion Anonymous Classes
}