package bachelor.intra.other;

import java.io.*;

class SubClassSerializable implements Serializable {

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    }

    public static void main(String[] args) throws Exception {
        SubClassSerializable obj = new SubClassSerializable();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        byte[] data = baos.toByteArray();
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            ois.readObject();
        }
    }
}
