package lk.susa.plant_lk.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import lk.susa.plant_lk.R;

public abstract class BaseFragment extends Fragment {

    private AlertDialog loadingDialog;

    public void showLoading() {
        if (getContext() == null) return;

        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
            builder.setView(view);
            builder.setCancelable(false);

            loadingDialog = builder.create();

            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }

        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }

        if (loadingDialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().density * 200);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            loadingDialog.getWindow().setLayout(width, height);
        }
    }

    public void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideLoading();
    }
}