package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.Models.User.PermissionLevel;
import com.Petcare.Petcare.Models.User.User;

public interface AccountService {

    void updateUserPermissions(User currentUser, PermissionLevel level);
}
