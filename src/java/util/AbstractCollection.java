package java.util;

public abstract class AbstractCollection<E> implements Collection<E>{
    private static final int MAX_ARRAY_SIZE=Integer.MAX_VALUE-8;

    protected AbstractCollection(){
    }

    public String toString(){
        /*�ƶϷ��� E */
        Iterator<E> it=this.iterator();
        if(!it.hasNext()) return "����û������";
        StringBuilder sb=new StringBuilder();
        sb.append('[');
        for(;;){
            E e=it.next();
            sb.append(e==this?"��ǰ����":e);
            if(!it.hasNext()){
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }

    public abstract Iterator<E> iterator();

    public abstract int size();

    public boolean isEmpty(){
        return size()==0;
    }

    public boolean contains(Object o){
        //Iterator<E> it=this.iterator();
        Iterator<E> it=iterator();
        if(o==null){
             /*
              * �Ƿ����Ԫ��Ϊ���� null ����
              * */
            while(it.hasNext()){
                if(it.next()==null){
                    return true;
                }
            }
        }else{
            /*
              * �Ƿ����Ԫ��Ϊ������ null ����
              * */
            while(it.hasNext()){
                if(o.equals(it.next())){
                    return true;
                }
            }
        }
        return false;
    }

    public Object[] toArray(){
        //׼���������������
        //Object[] r=new Object[this.size()];
        //Iterator<E> it=this.iterator();
        Object[] r=new Object[size()];
        Iterator<E> it=iterator();
        for(int i=0;i<r.length;i++){
            // �����ʵ��Դ��Ԫ�ظ���
            if(!it.hasNext()){
                return Arrays.copyOf(r,i);
            }
            r[i]=it.next();
        }
        return it.hasNext()?finishToArray(r,it):r;
    }

    /*
    * ����
    * String[] a = list.toArray(new String[list.length()]);
    * */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a){
        //׼���������������
        //int size=this.size();
        int size=size();
        /*
        * ����ʵ��������������飬�����ƶ�
        * */
        T[] r=a.length>=size?a:(T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(),size);
        //Iterator<E> it=this.iterator();
        Iterator<E> it=iterator();
        /*
        * ��������Դ���ϣ������ʵ��Դ��Ԫ�ؿ��ܸ���
        * */
        for(int i=0;i<r.length;i++){
            /*
            * ������Ҫ���������м�Դ������
            * ���в�ȷ��Դ��Ŀ��˭�Ķ���������
            * ���һֱ��Դ�����������δ�����������жϣ�˵���ǲ���Դ������
            * �������Ŀ���������������������жϣ���Ϊ�ľ�Դ����ƥ����������
            * �����α�ָ���ǽ������������ԴԪ��
            * */
            if(!it.hasNext()){
                if(a==r){
                    /*
                    * ������Դ�����ľ�
                    * ����Ŀ����ʱ��ͽ�����Ŀ��Դ�䵱�м�Դ������һ�£�����ͬʱԴԪ��Ϊ�գ�����ΪĿ�����������null
                    * ����� null ���м�Դ��������ֵ
                    * */
                    r[i]=null;
                }else if(a.length<i){
                    /*
                    * ������Դ�ľ���Ŀ������С�����������������д����
                    * Ŀ�곤��С��Դ����Դ���䣬����Դ��������
                    * */
                    return Arrays.copyOf(r,i);
                }else{
                    /*
                    * ���Ǹ���Դ��ȫ����Ŀ���ȫ����������Դ�����ֵ
                    * */
                    System.arraycopy(r,0,a,0,i);
                    /*
                    * ��Ϊ Դ�Ѿ��ľ���Ŀ��ֵ��Դ����λ��Ϊ null �����
                    * */
                    if(a.length>i){
                        a[i]=null;
                    }
                }
                /*
                * ����ֻ����Ŀ��������Դ������ת��ֵ������Ŀ�꼯��
                * */
                return a;
            }
            /*
            * ѭ��ת��ֵ��֪���ж��˳�
            * ֻ���������ŶԵ�������������
            * */
            r[i]=(T)it.next();
        }
        // more elements than expected
        return it.hasNext()?finishToArray(r,it):r;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] finishToArray(T[] r,Iterator<?> it){
        /*
        * Դ���鳤��
        * */
        int i=r.length;
        while(it.hasNext()){
            int cap=r.length;
            if(i==cap){
                int newCap=cap+(cap>>1)+1;
                // overflow-conscious code
                if(newCap-MAX_ARRAY_SIZE>0)
                    newCap=hugeCapacity(cap+1);
                r=Arrays.copyOf(r,newCap);
            }
            r[i++]=(T)it.next();
        }
        // trim if overallocated
        return (i==r.length)?r:Arrays.copyOf(r,i);
    }

    private static int hugeCapacity(int minCapacity){
        if(minCapacity<0){
            throw new OutOfMemoryError("��Ҫ�����ļ������");
        }
        return (minCapacity>MAX_ARRAY_SIZE)?Integer.MAX_VALUE:MAX_ARRAY_SIZE;
    }

    public boolean add(E e){
        throw new UnsupportedOperationException();
    }

    /*
    * �Ƴ�ԴĳһԪ��
    * */
    public boolean remove(Object o){
        //Iterator<E> it=this.iterator();
        Iterator<E> it=iterator();
        if(o==null){
             /*
              * �����Ƴ� ����Ϊ null �Ķ��󣬳ɹ��߷���true
              * */
            while(it.hasNext()){
                if(it.next()==null){
                    it.remove();
                    return true;
                }
            }
        }else{
            /*
            *  �����Ƴ� ����Ϊ �� null �Ķ��󣬳ɹ��߷���true
            * */
            while(it.hasNext()){
                if(o.equals(it.next())){
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }

    /*
    * Դ����Ŀ���һ��Ԫ��
    * */
    public boolean containsAll(Collection<?> c){
        /*
        * ��������ȥƥ�� this ��Դ����ֱ��ȫ��ƥ�䣬���� true
        * */
        for(Object e : c){
            //if(!this.contains(e)){
            if(!contains(e)){
                return false;
            }
        }
        return true;
    }

    /*
    * �������󼯺ϵ�������
    * ����Ĭ�ϼ̳е����߷���
    * */
    public boolean addAll(Collection<? extends E> c){
        /*
        * Ĭ������ʧ��
        * */
        boolean modified=false;
        /*
        * ����� E �ǵ����ߵ�ʵ������
        * ���������Ķ�����Ҫ����
        * */
        for(E e : c)
            if(add(e)){
                modified=true;
            }
        return modified;
    }

    /*
    * �Ƴ�����Ԫ��
    * */
    public boolean removeAll(Collection<?> c){
        /*
        * ������Ʒǿ�
        * */
        Objects.requireNonNull(c);
        /*
        * Ĭ���Ƴ�һ��ԴԪ��ʧ�ܣ����� false
        * */
        boolean modified=false;
        //Iterator<?> it=this.iterator();
        Iterator<?> it=iterator();
        while(it.hasNext()){
            if(c.contains(it.next())){
                it.remove();
                /*
                * �����Ƴ�ԴԪ�أ�ֱ�������ߺľ�������true
                * */
                modified=true;
            }
        }
        return modified;
    }

    /*
    * �Ƿ񱣳���ԴԪ��
    * */
    public boolean retainAll(Collection<?> c){
        /*
        * ������Ʒǿ�
        * */
        Objects.requireNonNull(c);
        /*
        * Ĭ�ϱ���ԴԪ�ط��� false
        * */
        boolean modified=false;
        //Iterator<E> it=this.iterator();
        /*
        * ���õ�ǰ�����ߵ���
        * */
        Iterator<E> it=iterator();
        while(it.hasNext()){
            if(!c.contains(it.next())){
                /*
                * ������Ϊ it.remove(); ����Ҫ ��removeAll ������Ҫ
                * */
                it.remove();
                /*
                * ƥ��ֱ���в�ͬ
                * */
                modified=true;
            }
        }
        return modified;
    }

    /*
    * �Ƴ�����Ԫ��
    * */
    public void clear(){
        Iterator<E> it=iterator();
        while(it.hasNext()){
            it.next();
            it.remove();
        }
    }
    //  String conversion
}
