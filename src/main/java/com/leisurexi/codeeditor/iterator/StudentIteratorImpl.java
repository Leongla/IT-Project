package com.leisurexi.codeeditor.iterator;

import java.util.List;

/**
 * @author hj
 * @date 2022/8/5
 */
public class StudentIteratorImpl implements StudentIterator{
    private List<Student> list;
    private int position = 0;

    public StudentIteratorImpl(List<Student> list){
        this.list = list;
    }
    @Override
    public boolean hasNext() {
        return position < list.size();
    }

    @Override
    public Student next() {
        Student currentStudent = list.get(position);
        position++;
        return currentStudent;
    }
}
