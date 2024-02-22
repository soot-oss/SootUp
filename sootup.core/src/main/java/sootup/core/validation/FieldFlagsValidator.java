package sootup.core.validation;

import java.util.List;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;

public class FieldFlagsValidator implements ClassValidator {

  @Override
  public void validate(SootClass sc, List<ValidationException> exceptions) {
    for (SootField sf : sc.getFields()) {
      if ((sf.isPrivate() || sf.isProtected()) && (sf.isPublic()) || sf.isProtected()) {
        exceptions.add(
            new ValidationException(
                sc,
                "Field $1 can only be either public, protected or private"
                    .replace("$1", sf.getName())));
      }

      if (sc.isInterface()) {
        if (!sf.isPublic()) {
          exceptions.add(
              new ValidationException(
                  sc, "Field $1 must be an interface and public".replace("$1", sf.getName())));
        }
        if (!sf.isStatic()) {
          exceptions.add(
              new ValidationException(
                  sc, "Field $1 must be an interface and static".replace("$1", sf.getName())));
        }
        if (!sf.isFinal()) {
          exceptions.add(
              new ValidationException(
                  sc, "Field $1 must be an interface and final".replace("$1", sf.getName())));
        }
      }
    }
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
