package sg.edu.tp.seanwong.musica.util;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.source.ShuffleOrder;

public class CustomShuffleOrder extends ShuffleOrder.DefaultShuffleOrder {
    int firstIndex;
    int lastIndex;
    public CustomShuffleOrder(int length, int firstIndex, int lastIndex) {
        super(length);
    }

    @Override
    public int getNextIndex(int index) {
        if (super.getNextIndex(index) == C.INDEX_UNSET) {
            // We're at the last shuffled index
            // Go to the first shuffled index
            return 0;
        } else if (index == getLength() - 1) {
            // Stop playback when at the last unshuffled index
            return C.INDEX_UNSET;
        } else {
            return super.getNextIndex(index);
        }
    }

    @Override
    public int getPreviousIndex(int index) {
        if (super.getPreviousIndex(index) == C.INDEX_UNSET) {
            // We're at the start of the shuffled indices
            return getLastIndex();
        } else if (index == 0) {
            // Stop playback when at the first unshuffled index
            return C.INDEX_UNSET;
        } else {
            return super.getNextIndex(index);
        }
    }

    @Override
    public int getLastIndex() {
        return lastIndex;
    }

    @Override
    public int getFirstIndex() {
        return firstIndex;
    }
}
