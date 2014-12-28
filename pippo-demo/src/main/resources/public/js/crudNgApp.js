/*
 * Copyright (C) 2014 the original author or authors.
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
var crudNgApp = angular.module("crudNgApp", []);
var baseUrl = angular.element("base").attr("href");

//
// Contacts Controller is responsible for listing and deleting a contact
// see crudng/contacts.ftl
//
crudNgApp.controller("ContactsCtrl", [ '$scope', '$http', '$location',
		'$window', function($scope, $http, $location, $window) {

			//
			// Retrieve the contacts from Pippo
			//
			$http({
				method : 'GET',
				url : baseUrl + 'api/contacts'
			}).success(function(data, status, headers, config) {
				// update the ng-models on success
				$scope.contacts = data;
			}).error(function(data, status, headers, config) {
				// alert on failure
				alert("Whoops!\n\n" + JSON.stringify({
					message : data.statusMessage,
					code : data.statusCode,
					method : data.requestMethod,
					uri : data.requestUri
				}, null, 2));
			});

			//
			// Delete the specified contact
			$scope.deleteContact = function(id) {
				$http({
					method : 'DELETE',
					url : baseUrl + 'api/contact/' + id
				}).success(function(data, status, headers, config) {
					// redirect on success
					$window.location.href = baseUrl;
				}).error(function(data, status, headers, config) {
					// alert on failure
					alert("Whoops!\n\n" + JSON.stringify({
						message : data.statusMessage,
						code : data.statusCode,
						method : data.requestMethod,
						uri : data.requestUri
					}, null, 2));
				});
			};
		} ]);

//
// Contact Controller is responsible for editing a contact
// see crudng/contact.ftl
//
crudNgApp.controller("ContactCtrl", [ '$scope', '$http', '$location',
		'$window', function($scope, $http, $location, $window) {

			// Extract contact id from the url
			var contactUrl = $location.absUrl();
			var id = contactUrl.substring(contactUrl.lastIndexOf('/') + 1);

			//
			// Retrieve the specified contact from Pippo
			//
			$http({
				method : 'GET',
				url : baseUrl + 'api/contact/' + id
			}).success(function(data, status, headers, config) {
				// update the ng-models on success
				$scope.id = data.id;
				$scope.name = data.name;
				$scope.phone = data.phone;
				$scope.address = data.address;
			}).error(function(data, status, headers, config) {
				// alert on failure
				alert("Whoops!\n\n" + JSON.stringify({
					message : data.statusMessage,
					code : data.statusCode,
					method : data.requestMethod,
					uri : data.requestUri
				}, null, 2));
			});

			//
			// Post the new/updated contact back to Pippo
			//
			$scope.postContact = function(isValid) {

				if (isValid) {
					// prepare the contact from the ng-models
					var dataObj = {
						id : $scope.id,
						name : $scope.name,
						phone : $scope.phone,
						address : $scope.address
					};

					$http({
						method : 'POST',
						url : baseUrl + 'api/contact',
						data : dataObj
					}).success(function(data, status, headers, config) {
						// redirect on success
						$window.location.href = baseUrl;
					}).error(function(data, status, headers, config) {
						// alert on failure
						alert("Whoops!\n\n" + JSON.stringify({
							message : data.statusMessage,
							code : data.statusCode,
							method : data.requestMethod,
							uri : data.requestUri
						}, null, 2));
					});
				}
			};
		} ]);
