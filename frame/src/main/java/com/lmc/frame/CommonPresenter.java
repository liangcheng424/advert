package com.lmc.frame;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.disposables.Disposable;

public class CommonPresenter<V extends ICommonView,M extends ICommonModel> implements ICommonPresenter {


    private SoftReference<V> view;
    private SoftReference<M> model;
    private List<Disposable> mDisposableList;

    //构造中，接收view和model的对象
    public CommonPresenter(V pView, M pModel) {
        mDisposableList = new ArrayList<>();
        view = new SoftReference<>(pView);//软引用包裹，当内存不足的时候确保能够回收，避免内存溢出
        model = new SoftReference<>(pModel);

    }

    /**
     * 发起普通的网络请求
     * @param whichApi   接口标识
     * @param pPS    作为公共封装的方法，并不知道未来传递的是什么类型的参数，也不知道会传递多少个，所以通过泛型可变参数来声明形参
     */
    @Override
    public void getData(int whichApi, Object... pPS) {
        if(model!=null&&model.get()!=null)model.get().getData(this, whichApi, pPS);
    }

    /**
     * 将所有网络请求的订阅关系，统一到中间层的集合中，用于view销毁时，统一处理
     * @param pDisposable
     */
    @Override
    public void addObserver(Disposable pDisposable) {
        if(mDisposableList == null)return;
        mDisposableList.add(pDisposable);
    }

    @Override
    public void onSuccess(int whichApi, Object... pD) {
        if(view!=null&&view.get()!=null)view.get().onSuccess(whichApi, pD);
    }

    @Override
    public void onFailed(int whichApi, Throwable pThrowable) {
        if(view!=null&&view.get()!=null)view.get().onFailed(whichApi, pThrowable);
    }

    /**
     * 当activity页面销毁执行ondestroy时，这个时候已经没有展示数据的需求了，所以首先要将该页面进行的所有网络请求终止，
     * 以免gc回收时发现view仍被持有不能回收导致内存泄漏。当然这个即使不处理，这个泄漏时间会很短暂，当gc线程下一次检测
     * 到该对象，网络任务如果已完成，并不影响activity的回收
     *
     * 在MVP使用中，为了实现视图和数据的解耦，我们通过中间层presenter来进行双向绑定和处理，但当view销毁时，由于p层仍然
     * 持有view的引用，也可能会发生view不能被回收导致内存泄漏的情况，所以当页面销毁时，将p同view和m进行解绑
     */
    public void clear(){
        for(Disposable dis:mDisposableList){
            if(dis!=null&&!dis.isDisposed())dis.dispose();
        }
        if(view!=null){
            view.clear();;
            view=null;
        }

        if (model!=null){
            model.clear();
            model =null;
        }
    }
}
