public class FieldMerge {

    private String [] fCatalogsList = null;

    public FieldMerge(){
        init(new String[]{""}, true);
    }

    private void init (String [] catalogs, boolean preferPublic) {
        fCatalogsList = (catalogs != null) ? (String[]) catalogs.clone() : null;
    }
}