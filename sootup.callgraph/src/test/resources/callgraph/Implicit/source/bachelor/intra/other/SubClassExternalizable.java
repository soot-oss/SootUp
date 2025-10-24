package bachelor.intra.other;

import java.io.*;

class SuperClassExternalizable implements Externalizable {
    public SuperClassExternalizable() {
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }
}

class SubClassExternalizable extends SuperClassExternalizable implements Externalizable {
    public SubClassExternalizable() {
        super();
    }

    public static void main(String[] args) throws Exception {
        SubClassExternalizable obj = new SubClassExternalizable();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        byte[] data = baos.toByteArray();
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object deserialized = ois.readObject();
        }
    }
}
