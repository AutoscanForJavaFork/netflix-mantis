/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mantisrx.master.api.akka.route.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import scala.Tuple1;

public class ListObjectTests {

    private static Random rnd = new Random(System.currentTimeMillis());
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @Ignore
    public void testSortingByInvalidFieldName() {

        List<TestObject> objects = generateList(10);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Specified sort field is invalid.");
        new ListObject.Builder<TestObject>()
                .withObjects(objects, TestObject.class)
                .withSortField("invalidValue")
                .withSortAscending(true)
                .build();
    }

    @Test
    public void testSortingByNullFieldName() throws RuntimeException {
        ArrayList<TestObject> objects = generateList(10);

        // if not specifying sort field, the returned list should be in original order
        List<TestObject> list = new ListObject.Builder<TestObject>()
                .withObjects((List<TestObject>) objects.clone(), TestObject.class)
                .withSortField(null)
                .withSortAscending(true)
                .build().list;
        for (int i = 0; i < objects.size(); i++) {
            assert objects.get(i).publicValue == list.get(i).publicValue;
        }
    }

    @Test
    public void testSortingByEmptyFieldName() throws RuntimeException {
        ArrayList<TestObject> objects = generateList(10);

        // if not specifying sort field, the returned list should be in original order
        List<TestObject> list = new ListObject.Builder<TestObject>()
                .withObjects((List<TestObject>) objects.clone(), TestObject.class)
                .withSortField("")
                .withSortAscending(true)
                .build().list;
        for (int i = 0; i < objects.size(); i++) {
            assert objects.get(i).publicValue == list.get(i).publicValue;
        }
    }

    @Test
    public void testSortingByPublicValueFieldName() {

        List<TestObject> objects = generateList(10);
        List<TestObject> sortedList = new ListObject.Builder<TestObject>()
                .withObjects(objects, TestObject.class)
                .withSortField("publicValue")
                .withSortAscending(true)
                .build().list;

        assert sortedList.size() == objects.size();

        int prevValue = sortedList.get(0).publicValue;
        for (int i = 1; i < sortedList.size(); i++) {
            assert sortedList.get(i).publicValue >= prevValue;
            prevValue = sortedList.get(i).publicValue;
        }
    }

    @Test
    public void testSortingByPublicValueFieldNameDescending() {

        List<TestObject> objects = generateList(10);
        List<TestObject> sortedList = new ListObject.Builder<TestObject>()
                .withObjects(objects, TestObject.class)
                .withSortField("publicValue")
                .withSortAscending(false)
                .build().list;

        assert sortedList.size() == objects.size();

        int prevValue = sortedList.get(0).publicValue;
        for (int i = 1; i < sortedList.size(); i++) {
            assert sortedList.get(i).publicValue < prevValue;
            prevValue = sortedList.get(i).publicValue;
        }
    }

    @Test
    @Ignore
    public void testSortingByPrivateValueFieldName() {

        List<TestObject> objects = generateList(10);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Cannot access sort field.");
        new ListObject.Builder<TestObject>()
                .withObjects(objects, TestObject.class)
                .withSortField("privateValue")
                .withSortAscending(true)
                .build();
    }

    @Test
    public void testSortingByPrivateGetterValueFieldName() {

        List<TestObject> objects = generateList(10);
        List<TestObject> sortedList = new ListObject.Builder<TestObject>()
                .withObjects(objects, TestObject.class)
                .withSortField("privateGetterValue")
                .withSortAscending(true)
                .build().list;

        assert sortedList.size() == objects.size();

        int prevValue = sortedList.get(0).publicValue;
        for (int i = 1; i < sortedList.size(); i++) {
            assert sortedList.get(i).publicValue >= prevValue;
            prevValue = sortedList.get(i).publicValue;
        }
    }

    @Test
    public void testSortingByProtectedValueFieldName() {

        List<TestObject> objects = generateList(10);
        List<TestObject> sortedList = new ListObject.Builder<TestObject>()
                .withObjects(objects, TestObject.class)
                .withSortField("protectedValue")
                .withSortAscending(true)
                .build().list;

        assert sortedList.size() == objects.size();

        int prevValue = sortedList.get(0).publicValue;
        for (int i = 1; i < sortedList.size(); i++) {
            assert sortedList.get(i).publicValue >= prevValue;
            prevValue = sortedList.get(i).publicValue;
        }
    }


    @Test
    @Ignore
    public void testPaginationLimit() {

        List<TestObject> objects = generateList(10);

        assert (new ListObject.Builder<TestObject>()
                .withObjects(objects, TestObject.class)
                .withLimit(5)
                .build().list.size() == 5);
    }

    @Test
    @Ignore
    public void testPaginationInvalidLimit() {

        List<TestObject> objects = generateList(10);
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("limit needs to be greater than 0");

        new ListObject.Builder<TestObject>()
                        .withObjects(objects, TestObject.class)
                        .withLimit(-1)
                        .build().list.size();
    }

    @Test
    public void testPaginationLimitAndOffset() {

        List<TestObject> objects = generateList(10);

        List<TestObject> list = new ListObject.Builder<TestObject>()
                        .withObjects(objects, TestObject.class)
                        .withLimit(5)
                        .withOffset(1)
                        .build().list;
        assert list.size() == 5;

        for (int i =0; i< 5; i++) {
            assert list.get(i).publicValue == objects.get(i+1).publicValue;
        }
    }

    @Test
    public void testPaginationTooBigLimitAndOffset() {

        List<TestObject> objects = generateList(10);

        List<TestObject> list = new ListObject.Builder<TestObject>()
                .withObjects(objects, TestObject.class)
                .withLimit(5)
                .withOffset(6)
                .build().list;
        assert list.size() == 4;

        for (int i =0; i< 4; i++) {
            assert list.get(i).publicValue == objects.get(i+6).publicValue;
        }
    }


    @Test
    public void testPaginationTooBigLimitAndInvalidOffset() {

        List<TestObject> objects = generateList(10);

        List<TestObject> list = new ListObject.Builder<TestObject>()
                .withObjects(objects, TestObject.class)
                .withLimit(5)
                .withOffset(11)
                .build().list;
        assert list.size() == 0;
    }


    @Test
    public void testEmptyList() {

        List<TestObject> objects = new ArrayList<>();

        List<TestObject> list = new ListObject.Builder<TestObject>()
                .withObjects(objects, TestObject.class)
                .withOffset(0)
                .build().list;
        assert list.size() == 0;
    }


    private ArrayList<TestObject> generateList(int size) {
        assert size > 0;

        ArrayList<TestObject> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {

            list.add(new TestObject());
        }

        return list;
    }

    public class TestObject {
        private int privateValue;
        private int privateGetterValue;
        public int publicValue;
        protected int protectedValue;
        public Tuple1<Integer> complexTypeField;

        public TestObject() {
            int randomVal = rnd.nextInt() % 10000;
            this.privateValue = randomVal;
            this.privateGetterValue = randomVal;
            this.publicValue = randomVal;
            this.protectedValue = randomVal;
            this.complexTypeField = new Tuple1<>(randomVal);
        }

        public int getPrivateGetterValue() {
            return this.privateGetterValue;
        }
    }
}
