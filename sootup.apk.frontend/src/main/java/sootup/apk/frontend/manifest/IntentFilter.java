package sootup.apk.frontend.manifest;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * An {@code <intent-filter>} declared on a manifest component: the actions and categories it
 * declares itself able to handle. Used for future implicit-Intent (ICC) resolution.
 */
public final class IntentFilter {

  @NonNull private final List<String> actions;
  @NonNull private final List<String> categories;

  public IntentFilter(@NonNull List<String> actions, @NonNull List<String> categories) {
    this.actions = Collections.unmodifiableList(new ArrayList<>(actions));
    this.categories = Collections.unmodifiableList(new ArrayList<>(categories));
  }

  @NonNull
  public List<String> getActions() {
    return actions;
  }

  @NonNull
  public List<String> getCategories() {
    return categories;
  }

  @Override
  public String toString() {
    return "IntentFilter{actions=" + actions + ", categories=" + categories + '}';
  }
}
