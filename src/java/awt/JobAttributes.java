/**
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public final class JobAttributes implements Cloneable{
    private int copies;
    private DefaultSelectionType defaultSelection;
    private DestinationType destination;
    private DialogType dialog;
    private String fileName;
    private int fromPage;
    private int maxPage;
    private int minPage;
    private MultipleDocumentHandlingType multipleDocumentHandling;
    private int[][] pageRanges;
    private int prFirst;
    private int prLast;
    private String printer;
    private SidesType sides;
    private int toPage;

    public JobAttributes(){
        setCopiesToDefault();
        setDefaultSelection(DefaultSelectionType.ALL);
        setDestination(DestinationType.PRINTER);
        setDialog(DialogType.NATIVE);
        setMaxPage(Integer.MAX_VALUE);
        setMinPage(1);
        setMultipleDocumentHandlingToDefault();
        setSidesToDefault();
    }

    public void setCopiesToDefault(){
        setCopies(1);
    }

    public void setMultipleDocumentHandlingToDefault(){
        setMultipleDocumentHandling(
                MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_UNCOLLATED_COPIES);
    }

    public void setSidesToDefault(){
        setSides(SidesType.ONE_SIDED);
    }

    public JobAttributes(JobAttributes obj){
        set(obj);
    }

    public void set(JobAttributes obj){
        copies=obj.copies;
        defaultSelection=obj.defaultSelection;
        destination=obj.destination;
        dialog=obj.dialog;
        fileName=obj.fileName;
        fromPage=obj.fromPage;
        maxPage=obj.maxPage;
        minPage=obj.minPage;
        multipleDocumentHandling=obj.multipleDocumentHandling;
        // okay because we never modify the contents of pageRanges
        pageRanges=obj.pageRanges;
        prFirst=obj.prFirst;
        prLast=obj.prLast;
        printer=obj.printer;
        sides=obj.sides;
        toPage=obj.toPage;
    }

    public JobAttributes(int copies,DefaultSelectionType defaultSelection,
                         DestinationType destination,DialogType dialog,
                         String fileName,int maxPage,int minPage,
                         MultipleDocumentHandlingType multipleDocumentHandling,
                         int[][] pageRanges,String printer,SidesType sides){
        setCopies(copies);
        setDefaultSelection(defaultSelection);
        setDestination(destination);
        setDialog(dialog);
        setFileName(fileName);
        setMaxPage(maxPage);
        setMinPage(minPage);
        setMultipleDocumentHandling(multipleDocumentHandling);
        setPageRanges(pageRanges);
        setPrinter(printer);
        setSides(sides);
    }

    public int getCopies(){
        return copies;
    }

    public void setCopies(int copies){
        if(copies<=0){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "copies");
        }
        this.copies=copies;
    }

    public DefaultSelectionType getDefaultSelection(){
        return defaultSelection;
    }

    public void setDefaultSelection(DefaultSelectionType defaultSelection){
        if(defaultSelection==null){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "defaultSelection");
        }
        this.defaultSelection=defaultSelection;
    }

    public DestinationType getDestination(){
        return destination;
    }

    public void setDestination(DestinationType destination){
        if(destination==null){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "destination");
        }
        this.destination=destination;
    }

    public DialogType getDialog(){
        return dialog;
    }

    public void setDialog(DialogType dialog){
        if(dialog==null){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "dialog");
        }
        this.dialog=dialog;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFileName(String fileName){
        this.fileName=fileName;
    }

    public int getFromPage(){
        if(fromPage!=0){
            return fromPage;
        }else if(toPage!=0){
            return getMinPage();
        }else if(pageRanges!=null){
            return prFirst;
        }else{
            return getMinPage();
        }
    }

    public void setFromPage(int fromPage){
        if(fromPage<=0||
                (toPage!=0&&fromPage>toPage)||
                fromPage<minPage||
                fromPage>maxPage){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "fromPage");
        }
        this.fromPage=fromPage;
    }

    public int getMaxPage(){
        return maxPage;
    }

    public void setMaxPage(int maxPage){
        if(maxPage<=0||maxPage<minPage){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "maxPage");
        }
        this.maxPage=maxPage;
    }

    public int getMinPage(){
        return minPage;
    }

    public void setMinPage(int minPage){
        if(minPage<=0||minPage>maxPage){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "minPage");
        }
        this.minPage=minPage;
    }

    public MultipleDocumentHandlingType getMultipleDocumentHandling(){
        return multipleDocumentHandling;
    }

    public void setMultipleDocumentHandling(MultipleDocumentHandlingType
                                                    multipleDocumentHandling){
        if(multipleDocumentHandling==null){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "multipleDocumentHandling");
        }
        this.multipleDocumentHandling=multipleDocumentHandling;
    }

    public int[][] getPageRanges(){
        if(pageRanges!=null){
            // Return a copy because otherwise client code could circumvent the
            // the checks made in setPageRanges by modifying the returned
            // array.
            int[][] copy=new int[pageRanges.length][2];
            for(int i=0;i<pageRanges.length;i++){
                copy[i][0]=pageRanges[i][0];
                copy[i][1]=pageRanges[i][1];
            }
            return copy;
        }else if(fromPage!=0||toPage!=0){
            int fromPage=getFromPage();
            int toPage=getToPage();
            return new int[][]{new int[]{fromPage,toPage}};
        }else{
            int minPage=getMinPage();
            return new int[][]{new int[]{minPage,minPage}};
        }
    }

    public void setPageRanges(int[][] pageRanges){
        String xcp="Invalid value for attribute pageRanges";
        int first=0;
        int last=0;
        if(pageRanges==null){
            throw new IllegalArgumentException(xcp);
        }
        for(int i=0;i<pageRanges.length;i++){
            if(pageRanges[i]==null||
                    pageRanges[i].length!=2||
                    pageRanges[i][0]<=last||
                    pageRanges[i][1]<pageRanges[i][0]){
                throw new IllegalArgumentException(xcp);
            }
            last=pageRanges[i][1];
            if(first==0){
                first=pageRanges[i][0];
            }
        }
        if(first<minPage||last>maxPage){
            throw new IllegalArgumentException(xcp);
        }
        // Store a copy because otherwise client code could circumvent the
        // the checks made above by holding a reference to the array and
        // modifying it after calling setPageRanges.
        int[][] copy=new int[pageRanges.length][2];
        for(int i=0;i<pageRanges.length;i++){
            copy[i][0]=pageRanges[i][0];
            copy[i][1]=pageRanges[i][1];
        }
        this.pageRanges=copy;
        this.prFirst=first;
        this.prLast=last;
    }

    public String getPrinter(){
        return printer;
    }

    public void setPrinter(String printer){
        this.printer=printer;
    }

    public SidesType getSides(){
        return sides;
    }

    public void setSides(SidesType sides){
        if(sides==null){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "sides");
        }
        this.sides=sides;
    }

    public int getToPage(){
        if(toPage!=0){
            return toPage;
        }else if(fromPage!=0){
            return fromPage;
        }else if(pageRanges!=null){
            return prLast;
        }else{
            return getMinPage();
        }
    }

    public void setToPage(int toPage){
        if(toPage<=0||
                (fromPage!=0&&toPage<fromPage)||
                toPage<minPage||
                toPage>maxPage){
            throw new IllegalArgumentException("Invalid value for attribute "+
                    "toPage");
        }
        this.toPage=toPage;
    }

    public int hashCode(){
        int rest=((copies+fromPage+maxPage+minPage+prFirst+prLast+
                toPage)*31)<<21;
        if(pageRanges!=null){
            int sum=0;
            for(int i=0;i<pageRanges.length;i++){
                sum+=pageRanges[i][0]+pageRanges[i][1];
            }
            rest^=(sum*31)<<11;
        }
        if(fileName!=null){
            rest^=fileName.hashCode();
        }
        if(printer!=null){
            rest^=printer.hashCode();
        }
        return (defaultSelection.hashCode()<<6^
                destination.hashCode()<<5^
                dialog.hashCode()<<3^
                multipleDocumentHandling.hashCode()<<2^
                sides.hashCode()^
                rest);
    }

    public boolean equals(Object obj){
        if(!(obj instanceof JobAttributes)){
            return false;
        }
        JobAttributes rhs=(JobAttributes)obj;
        if(fileName==null){
            if(rhs.fileName!=null){
                return false;
            }
        }else{
            if(!fileName.equals(rhs.fileName)){
                return false;
            }
        }
        if(pageRanges==null){
            if(rhs.pageRanges!=null){
                return false;
            }
        }else{
            if(rhs.pageRanges==null||
                    pageRanges.length!=rhs.pageRanges.length){
                return false;
            }
            for(int i=0;i<pageRanges.length;i++){
                if(pageRanges[i][0]!=rhs.pageRanges[i][0]||
                        pageRanges[i][1]!=rhs.pageRanges[i][1]){
                    return false;
                }
            }
        }
        if(printer==null){
            if(rhs.printer!=null){
                return false;
            }
        }else{
            if(!printer.equals(rhs.printer)){
                return false;
            }
        }
        return (copies==rhs.copies&&
                defaultSelection==rhs.defaultSelection&&
                destination==rhs.destination&&
                dialog==rhs.dialog&&
                fromPage==rhs.fromPage&&
                maxPage==rhs.maxPage&&
                minPage==rhs.minPage&&
                multipleDocumentHandling==rhs.multipleDocumentHandling&&
                prFirst==rhs.prFirst&&
                prLast==rhs.prLast&&
                sides==rhs.sides&&
                toPage==rhs.toPage);
    }

    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            // Since we implement Cloneable, this should never happen
            throw new InternalError(e);
        }
    }

    public String toString(){
        int[][] pageRanges=getPageRanges();
        String prStr="[";
        boolean first=true;
        for(int i=0;i<pageRanges.length;i++){
            if(first){
                first=false;
            }else{
                prStr+=",";
            }
            prStr+=pageRanges[i][0]+":"+pageRanges[i][1];
        }
        prStr+="]";
        return "copies="+getCopies()+",defaultSelection="+
                getDefaultSelection()+",destination="+getDestination()+
                ",dialog="+getDialog()+",fileName="+getFileName()+
                ",fromPage="+getFromPage()+",maxPage="+getMaxPage()+
                ",minPage="+getMinPage()+",multiple-document-handling="+
                getMultipleDocumentHandling()+",page-ranges="+prStr+
                ",printer="+getPrinter()+",sides="+getSides()+",toPage="+
                getToPage();
    }

    public static final class DefaultSelectionType extends AttributeValue{
        private static final int I_ALL=0;
        public static final DefaultSelectionType ALL=
                new DefaultSelectionType(I_ALL);
        private static final int I_RANGE=1;
        public static final DefaultSelectionType RANGE=
                new DefaultSelectionType(I_RANGE);
        private static final int I_SELECTION=2;
        public static final DefaultSelectionType SELECTION=
                new DefaultSelectionType(I_SELECTION);
        private static final String NAMES[]={
                "all","range","selection"
        };

        private DefaultSelectionType(int type){
            super(type,NAMES);
        }
    }

    public static final class DestinationType extends AttributeValue{
        private static final int I_FILE=0;
        public static final DestinationType FILE=
                new DestinationType(I_FILE);
        private static final int I_PRINTER=1;
        public static final DestinationType PRINTER=
                new DestinationType(I_PRINTER);
        private static final String NAMES[]={
                "file","printer"
        };

        private DestinationType(int type){
            super(type,NAMES);
        }
    }

    public static final class DialogType extends AttributeValue{
        private static final int I_COMMON=0;
        public static final DialogType COMMON=new DialogType(I_COMMON);
        private static final int I_NATIVE=1;
        public static final DialogType NATIVE=new DialogType(I_NATIVE);
        private static final int I_NONE=2;
        public static final DialogType NONE=new DialogType(I_NONE);
        private static final String NAMES[]={
                "common","native","none"
        };

        private DialogType(int type){
            super(type,NAMES);
        }
    }

    public static final class MultipleDocumentHandlingType extends
            AttributeValue{
        private static final int I_SEPARATE_DOCUMENTS_COLLATED_COPIES=0;
        public static final MultipleDocumentHandlingType
                SEPARATE_DOCUMENTS_COLLATED_COPIES=
                new MultipleDocumentHandlingType(
                        I_SEPARATE_DOCUMENTS_COLLATED_COPIES);
        private static final int I_SEPARATE_DOCUMENTS_UNCOLLATED_COPIES=1;
        public static final MultipleDocumentHandlingType
                SEPARATE_DOCUMENTS_UNCOLLATED_COPIES=
                new MultipleDocumentHandlingType(
                        I_SEPARATE_DOCUMENTS_UNCOLLATED_COPIES);
        private static final String NAMES[]={
                "separate-documents-collated-copies",
                "separate-documents-uncollated-copies"
        };

        private MultipleDocumentHandlingType(int type){
            super(type,NAMES);
        }
    }

    public static final class SidesType extends AttributeValue{
        private static final int I_ONE_SIDED=0;
        public static final SidesType ONE_SIDED=new SidesType(I_ONE_SIDED);
        private static final int I_TWO_SIDED_LONG_EDGE=1;
        public static final SidesType TWO_SIDED_LONG_EDGE=
                new SidesType(I_TWO_SIDED_LONG_EDGE);
        private static final int I_TWO_SIDED_SHORT_EDGE=2;
        public static final SidesType TWO_SIDED_SHORT_EDGE=
                new SidesType(I_TWO_SIDED_SHORT_EDGE);
        private static final String NAMES[]={
                "one-sided","two-sided-long-edge","two-sided-short-edge"
        };

        private SidesType(int type){
            super(type,NAMES);
        }
    }
}
