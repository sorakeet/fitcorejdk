package java.util;

public abstract class AbstractCollection<E> implements Collection<E>{
    private static final int MAX_ARRAY_SIZE=Integer.MAX_VALUE-8;

    protected AbstractCollection(){
    }

    public String toString(){
        /*推断泛型 E */
        Iterator<E> it=this.iterator();
        if(!it.hasNext()) return "集合没有内容";
        StringBuilder sb=new StringBuilder();
        sb.append('[');
        for(;;){
            E e=it.next();
            sb.append(e==this?"当前集合":e);
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
              * 是否包含元素为类型 null 对象
              * */
            while(it.hasNext()){
                if(it.next()==null){
                    return true;
                }
            }
        }else{
            /*
              * 是否包含元素为非类型 null 对象
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
        //准备容量相符的数组
        //Object[] r=new Object[this.size()];
        //Iterator<E> it=this.iterator();
        Object[] r=new Object[size()];
        Iterator<E> it=iterator();
        for(int i=0;i<r.length;i++){
            // 这里比实际源的元素更少
            if(!it.hasNext()){
                return Arrays.copyOf(r,i);
            }
            r[i]=it.next();
        }
        return it.hasNext()?finishToArray(r,it):r;
    }

    /*
    * 例如
    * String[] a = list.toArray(new String[list.length()]);
    * */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a){
        //准备容量相符的数组
        //int size=this.size();
        int size=size();
        /*
        * 根据实际情况生成新数组，类型推断
        * */
        T[] r=a.length>=size?a:(T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(),size);
        //Iterator<E> it=this.iterator();
        Iterator<E> it=iterator();
        /*
        * 逐条操作源集合，这里比实际源的元素可能更少
        * */
        for(int i=0;i<r.length;i++){
            /*
            * 这里需要逐条操作中间源最大次数
            * 其中不确定源和目标谁的定义容量大
            * 如果一直到源逐条操作完毕未曾进入以下判断，说明是采用源最大次数
            * 如果采用目标最大操作次数，将进入判断，因为耗尽源，不匹配最大操作数
            * 这里游标指的是接下来将处理的源元素
            * */
            if(!it.hasNext()){
                if(a==r){
                    /*
                    * 现在是源迭代耗尽
                    * 在三目运算时候就将返回目标源充当中间源，引用一致，但是同时源元素为空，容量为目标容量，填充null
                    * 则填充 null 到中间源冗余索引值
                    * */
                    r[i]=null;
                }else if(a.length<i){
                    /*
                    * 这里是源耗尽，目标容量小于最大操作数，这里有待理解
                    * 目标长度小于源，则将源扩充，返回源容量集合
                    * */
                    return Arrays.copyOf(r,i);
                }else{
                    /*
                    * 这是复制源的全部到目标的全部，容量是源的最大值
                    * */
                    System.arraycopy(r,0,a,0,i);
                    /*
                    * 因为 源已经耗尽，目标值在源索引位置为 null ，填充
                    * */
                    if(a.length>i){
                        a[i]=null;
                    }
                }
                /*
                * 这里只可能目标容量比源容量大，转移值，返回目标集合
                * */
                return a;
            }
            /*
            * 循环转移值，知道判断退出
            * 只有相对这里才对迭代器减除计数
            * */
            r[i]=(T)it.next();
        }
        // more elements than expected
        return it.hasNext()?finishToArray(r,it):r;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] finishToArray(T[] r,Iterator<?> it){
        /*
        * 源数组长度
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
            throw new OutOfMemoryError("需要创建的集合溢出");
        }
        return (minCapacity>MAX_ARRAY_SIZE)?Integer.MAX_VALUE:MAX_ARRAY_SIZE;
    }

    public boolean add(E e){
        throw new UnsupportedOperationException();
    }

    /*
    * 移除源某一元素
    * */
    public boolean remove(Object o){
        //Iterator<E> it=this.iterator();
        Iterator<E> it=iterator();
        if(o==null){
             /*
              * 这里移除 类型为 null 的对象，成功者返回true
              * */
            while(it.hasNext()){
                if(it.next()==null){
                    it.remove();
                    return true;
                }
            }
        }else{
            /*
            *  这里移除 类型为 非 null 的对象，成功者返回true
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
    * 源包含目标的一切元素
    * */
    public boolean containsAll(Collection<?> c){
        /*
        * 逐条消耗去匹配 this （源），直到全部匹配，返回 true
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
    * 新增对象集合到调用者
    * 这里默认继承调用者泛型
    * */
    public boolean addAll(Collection<? extends E> c){
        /*
        * 默认新增失败
        * */
        boolean modified=false;
        /*
        * 这里的 E 是调用者的实际类型
        * 逐条操作的对象需要符合
        * */
        for(E e : c)
            if(add(e)){
                modified=true;
            }
        return modified;
    }

    /*
    * 移除所有元素
    * */
    public boolean removeAll(Collection<?> c){
        /*
        * 这里控制非空
        * */
        Objects.requireNonNull(c);
        /*
        * 默认移除一切源元素失败，返回 false
        * */
        boolean modified=false;
        //Iterator<?> it=this.iterator();
        Iterator<?> it=iterator();
        while(it.hasNext()){
            if(c.contains(it.next())){
                it.remove();
                /*
                * 逐条移除源元素，直到调用者耗尽，返回true
                * */
                modified=true;
            }
        }
        return modified;
    }

    /*
    * 是否保持了源元素
    * */
    public boolean retainAll(Collection<?> c){
        /*
        * 这里控制非空
        * */
        Objects.requireNonNull(c);
        /*
        * 默认保持源元素返回 false
        * */
        boolean modified=false;
        //Iterator<E> it=this.iterator();
        /*
        * 启用当前调用者迭代
        * */
        Iterator<E> it=iterator();
        while(it.hasNext()){
            if(!c.contains(it.next())){
                /*
                * 个人认为 it.remove(); 不需要 ，removeAll 倒是需要
                * */
                it.remove();
                /*
                * 匹配直到有不同
                * */
                modified=true;
            }
        }
        return modified;
    }

    /*
    * 移除所有元素
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
