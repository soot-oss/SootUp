import java.util.ArrayList;

public class SimpleCollectionTaint {
    // see Issue #519 - Thanks to "Y4er"
        int i = 1;
        int j = i;
        k = i;
        String a = "asd";
        String b = "asd";
        ArrayList<String> list = new ArrayList<>();
        list.add(a);
    }
}