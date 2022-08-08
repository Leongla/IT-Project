package com.leisurexi.codeeditor.iterator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hj
 * @date 2022/8/5
 */
public class StudentAggregateImpl implements StudentAggregate {
    private List<Student> list = new ArrayList<>();

    @Override
    public void addStudent(Student student) {
        this.list.add(student);
    }

    @Override
    public void removeStudent(Student student) {
        this.list.remove(student);
    }

    @Override
    public StudentIterator getStudentIterator() {
        return new StudentIteratorImpl(list);
    }
}
