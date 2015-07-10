/*
 * Copyright (C) 2015 the original author or authors.
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
function ViewModel() {
    var self = this;

    self.editedContact = {
        id: ko.observable(),
        name: ko.observable(),
        phone: ko.observable(),
        address: ko.observable()
    };
    self.contacts = ko.observableArray();

    self.initContacts = function () {
        $.getJSON("api/contacts", function(data) {
            self.contacts(data);
        });
    };

    self.addContact = function() {
        // clear previous values for `editedContact`
        self.editedContact.id(-1);
        self.editedContact.name('');
        self.editedContact.phone('');
        self.editedContact.address('');

        // show the modal with the edit form
        bootbox
            .dialog({
                title: 'Add contact',
                message: $('#editForm'),
                show: false, // we will show it manually later
                buttons: {
                    success: {
                        label: "Save",
                        className: "btn-success",
                        callback: function () {
                            self.saveContact(self.editedContact);
                        }
                    }
                }
            })
            .on('shown.bs.modal', function() {
                $('#editForm').show(); // show the edit form
            })
            .on('hide.bs.modal', function() {
                // Bootbox will remove the modal (including the body which contains the edit form)
                // after hiding the modal
                // Therefor, we need to backup the form
                $('#editForm').hide().appendTo('body');
            })
            .modal('show');
    };

    self.saveContact = function(contact) {
        $.ajax({
            type: "POST",
            url: "api/contact",
            data: {
                id: contact.id,
                name: contact.name,
                phone: contact.phone,
                address: contact.address
            },
            success: function() {
                self.initContacts();
            },
            error: function(msg) {
                console.log(msg);
            }
        });
    };

    self.deleteContact = function(contact) {
        // show the confirm dialog
        bootbox.confirm("Delete contact '" + contact.name + "'?", function(result) {
            if (result) {
                $.ajax({
                    type: "DELETE",
                    url: 'api/contact/' + contact.id,
                    success: function () {
                        self.initContacts();
                    },
                    error: function (msg) {
                        console.log(msg);
                    }
                });
            }
        });
    };

    self.editContact = function(contact) {
        // init `editedContact`
        self.editedContact.id(contact.id);
        self.editedContact.name(contact.name);
        self.editedContact.phone(contact.phone);
        self.editedContact.address(contact.address);

        // show the modal with the edit form
        bootbox
            .dialog({
                title: 'Edit contact',
                message: $('#editForm'),
                show: false, // we will show it manually later
                buttons: {
                    success: {
                        label: "Save",
                        className: "btn-success",
                        callback: function () {
                            self.saveContact(self.editedContact);
                        }
                    }
                }
            })
            .on('shown.bs.modal', function() {
                $('#editForm').show(); // show the edit form
            })
            .on('hide.bs.modal', function() {
                // Bootbox will remove the modal (including the body which contains the edit form)
                // after hiding the modal
                // Therefor, we need to backup the form
                $('#editForm').hide().appendTo('body');
            })
            .modal('show');
    };
}

$(document).ready(function() {
    var viewModel = new ViewModel();
    viewModel.initContacts();
    ko.applyBindings(viewModel);
});
