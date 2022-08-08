package com.leisurexi.codeeditor.iterator;

/**
 * @author hj
 * @date 2022/8/5
 */
public interface StudentAggregate {
    void addStudent(Student student);

    void removeStudent(Student student);

    StudentIterator getStudentIterator();
}
