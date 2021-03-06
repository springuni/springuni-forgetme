package com.springuni.forgetme.core.adapter;

import java.util.Optional;

public interface DataHandlerRegistry {

  Optional<DataHandlerRegistration> lookup(String name);

  Optional<DataHandlerRegistration> lookup(String name, boolean active);

}
