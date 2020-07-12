package sg.edu.tp.seanwong.musica.ui.now_playing;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NowPlayingViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public NowPlayingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("2");
    }

    public LiveData<String> getText() {
        return mText;
    }
}