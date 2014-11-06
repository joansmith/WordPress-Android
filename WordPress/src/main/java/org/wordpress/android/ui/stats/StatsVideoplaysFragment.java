package org.wordpress.android.ui.stats;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.wordpress.android.R;
import org.wordpress.android.ui.stats.model.TopPostModel;
import org.wordpress.android.ui.stats.model.VideoPlaysModel;
import org.wordpress.android.ui.stats.service.StatsService;
import org.wordpress.android.util.FormatUtils;

import java.util.List;


public class StatsVideoplaysFragment extends StatsAbstractListFragment {
    public static final String TAG = StatsVideoplaysFragment.class.getSimpleName();

    @Override
    protected void updateUI() {
        if (mDatamodel != null && ((VideoPlaysModel) mDatamodel).getPlays() != null
                && ((VideoPlaysModel) mDatamodel).getPlays().size() > 0) {
            List<TopPostModel> postViews = ((VideoPlaysModel) mDatamodel).getPlays();
            ArrayAdapter adapter = new TopPostsAndPagesAdapter(getActivity(), postViews);
            StatsUIHelper.reloadLinearLayout(getActivity(), adapter, mList);
            showEmptyUI(false);
        } else {
            showEmptyUI(true);
        }
    }

    @Override
    protected boolean isExpandableList() {
        return false;
    }

    private class TopPostsAndPagesAdapter extends ArrayAdapter<TopPostModel> {

        private final List<TopPostModel> list;
        private final Activity context;
        private final LayoutInflater inflater;

        public TopPostsAndPagesAdapter(Activity context, List<TopPostModel> list) {
            super(context, R.layout.stats_list_cell, list);
            this.context = context;
            this.list = list;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            // reuse views
            if (rowView == null) {
                rowView = inflater.inflate(R.layout.stats_list_cell, null);
                // configure view holder
                StatsViewHolder viewHolder = new StatsViewHolder(rowView);
                rowView.setTag(viewHolder);
            }

            final TopPostModel currentRowData = list.get(position);
            StatsViewHolder holder = (StatsViewHolder) rowView.getTag();
            // fill data
            // entries
            holder.setEntryTextOrLink(currentRowData.getUrl(), currentRowData.getTitle());

            // totals
            holder.totalsTextView.setText(FormatUtils.formatDecimal(currentRowData.getViews()));

            // no icon
            holder.networkImageView.setVisibility(View.GONE);

            return rowView;
        }
    }

    @Override
    protected int getEntryLabelResId() {
        return R.string.stats_entry_video_plays;
    }

    @Override
    protected int getTotalsLabelResId() {
        return R.string.stats_totals_plays;
    }

    @Override
    protected int getEmptyLabelTitleResId() {
        return R.string.stats_empty_video;
    }

    @Override
    protected int getEmptyLabelDescResId() {
        return R.string.stats_empty_video_desc;
    }

    @Override
    protected StatsService.StatsEndpointsEnum getSectionToUpdate() {
        return StatsService.StatsEndpointsEnum.VIDEO_PLAYS;
    }

    @Override
    public String getTitle() {
        return getString(R.string.stats_view_video_plays);
    }
}