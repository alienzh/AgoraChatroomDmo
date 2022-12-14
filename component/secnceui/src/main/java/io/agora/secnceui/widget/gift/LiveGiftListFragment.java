package io.agora.secnceui.widget.gift;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.baseui.BaseInitFragment;
import io.agora.baseui.interfaces.OnItemClickListener;
import io.agora.secnceui.R;
import io.agora.secnceui.bean.GiftBean;
import io.agora.secnceui.utils.DeviceUtils;
import io.agora.secnceui.widget.recyclerview.HorizontalPageLayoutManager;
import io.agora.secnceui.widget.recyclerview.PagingScrollHelper;

public class LiveGiftListFragment extends BaseInitFragment implements OnItemClickListener {
    private RecyclerView rvList;
    private GiftListAdapter adapter;
    private GiftBean giftBean;
    private OnConfirmClickListener listener;
    private int position;

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle data = getArguments();
        if (null != data) position = data.getInt("position");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.chatroom_gift_fragment_list_layout;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        rvList = findViewById(R.id.rv_list);

        PagingScrollHelper snapHelper = new PagingScrollHelper();
        HorizontalPageLayoutManager manager = new HorizontalPageLayoutManager(1, 4);
        rvList.setHasFixedSize(true);
        rvList.setLayoutManager(manager);

        //设置item 间距
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setSize((int) DeviceUtils.dp2px(getContext(), 3),0 );
        itemDecoration.setDrawable(drawable);
        rvList.addItemDecoration(itemDecoration);

        adapter = new GiftListAdapter();
        rvList.setAdapter(adapter);

        snapHelper.setUpRecycleView(rvList);
        snapHelper.updateLayoutManger();
        snapHelper.scrollToPosition(0);
        rvList.setHorizontalScrollBarEnabled(true);
    }

    @Override
    protected void initListener() {
        super.initListener();
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.setData(GiftRepository.getGiftsByPage(getContext(),position));
        if (listener != null && adapter.getData() != null && adapter.mData.size() > 0){
            adapter.setSelectedPosition(0);
            listener.onFirstItem(adapter.getItem(0));
        }
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void onItemClick(View view, int position) {
        giftBean = adapter.getItem(position);
        boolean checked = giftBean.isChecked();
        giftBean.setChecked(!checked);
        if(giftBean.isChecked()) {
            adapter.setSelectedPosition(position);
        }else {
            adapter.setSelectedPosition(-1);
        }
        if (listener != null)
        listener.onConfirmClick(view,giftBean);
    }

    public void setOnItemSelectClickListener(OnConfirmClickListener listener) {
        this.listener = listener;
    }

}
