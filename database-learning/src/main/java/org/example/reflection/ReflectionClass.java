package org.example.reflection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReflectionClass extends BaseClass implements BaseInterface {

  private String privateString;
  protected String protectedString;
  public String publicString;

  private String privateMethod() {
    return "Private method output - very sensitive!";
  }

}
