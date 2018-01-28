package io.github.jsonSnapshot;

import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FakeObject {

    private String id;

    private Integer value;

    private String name;

    @Setter
    private FakeObject fakeObject;

    public void fakeMethod(String fakeName, Long fakeNumber, List<String> fakeList) {

    }

    public void fakeMethodWithComplexObject(Object fakeObj) {

    }

    public void fakeMethodWithComplexFakeObject(FakeObject fakeObj) {

    }
}
