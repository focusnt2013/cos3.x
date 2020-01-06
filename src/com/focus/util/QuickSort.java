package com.focus.util;

import java.util.List;

public abstract class QuickSort implements Sort
{
	public static final int ASC = 0;
	public static final int DESC = 1;
	private String orderby;
	private int sc = 0; 
	
	public QuickSort()
	{
	}
	public QuickSort(String orderby, int sc)
	{
		this.orderby = orderby;
		this.sc = sc;
	}
    /**
     * 定义的抽象接口
     * @param sortSrc Object
     * @param pivot Object
     * @return boolean sortSrc > pivot 返回true sortSrc <= pivot 返回false
     */
    public abstract boolean compareTo( Object sortSrc, Object pivot );

    /**
     * 比较数据，返回结果
     */
    private int partition( List sortAry, int low, int high )
    {
        Object pivot;
        int p_pos, i;
        p_pos = low;
        pivot = sortAry.get( p_pos );

        for( i = low + 1; i <= high; i++ )
        {
            if( compareTo( sortAry.get( i ), pivot ) )
            {
                p_pos++;
                swap( sortAry, p_pos, i );
            }
        }
        swap( sortAry, low, p_pos );

        return p_pos;
    }

    /**
     * 比较数据，返回结果
     */
    private int partition( Object sortAry[], int low, int high )
    {
        Object pivot;
        int p_pos, i;
        p_pos = low;
        pivot = sortAry[p_pos];

        for( i = low + 1; i <= high; i++ )
        {
            if( compareTo( sortAry[i], pivot ) )
            {
                p_pos++;
                swap( sortAry, p_pos, i );
            }
        }
        swap( sortAry, low, p_pos );

        return p_pos;
    }

    /**
     *
     */
    private void swap( Object sortAry[], int i, int j )
    {
        Object tmp = sortAry[i];
        sortAry[i] = sortAry[j];
        sortAry[j] = tmp;
    }

    /**
     *
     */
    private void swap( List sortAry, int i, int j )
    {
        Object tmp = sortAry.get( i );
        sortAry.set( i, sortAry.get( j ) );
        sortAry.set( j, tmp );
    }

    /**
     * 快速排序接口
     */
    public void sort( List sortAry )
    {
        quicksort( sortAry, 0, sortAry.size() - 1 );
    }

    /**
     * 快速排序接口
     */
    public void sort( Object sortAry[] )
    {
        quicksort( sortAry, 0, sortAry.length - 1 );
    }

    /**
     * 快速排序接口
     */
    private void quicksort( Object sortAry[], int low, int high )
    {
        int pivot;
        if( low < high )
        {
            pivot = partition( sortAry, low, high );
            quicksort( sortAry, low, pivot - 1 );
            quicksort( sortAry, pivot + 1, high );
        }
    }

    /**
     * 快速排序接口
     */
    private void quicksort( List sortAry, int low, int high )
    {
        int pivot;
        if( low < high )
        {
            pivot = partition( sortAry, low, high );
            quicksort( sortAry, low, pivot - 1 );
            quicksort( sortAry, pivot + 1, high );
        }
    }

    /**
     * 交换排序的位置
     */
    private static void swap( int a[], int i, int j )
    {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    /**
     *
     */
    private static int partition( int a[], int low, int high )
    {
        int pivot, p_pos, i;
        p_pos = low;
        pivot = a[p_pos];

        for( i = low + 1; i <= high; i++ )
        {
            if( a[i] > pivot )
            {
                p_pos++;
                swap( a, p_pos, i );
            }
        }
        swap( a, low, p_pos );

        return p_pos;
    }

    /**
     *
     */
    public static void quicksort( int a[], int low, int high )
    {
        int pivot;
        if( low < high )
        {
            pivot = partition( a, low, high );
            quicksort( a, low, pivot - 1 );
            quicksort( a, pivot + 1, high );
        }
    }
	public String getOrderby()
	{
		return orderby;
	}
	public void setOrderby(String orderby)
	{
		this.orderby = orderby;
	}
	public int getSc()
	{
		return sc;
	}
	public void setSc(int sc)
	{
		this.sc = sc;
	}
}
