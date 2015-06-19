<#import "base.ftl" as base/>
<@base.page title="Contact">
    <div class="page-header">
        <h2>Contact <small>RESTful JSON APIs + AngularJS</small></h2>
    </div>

    <div ng-controller="ContactCtrl">
      <form novalidate class="form-horizontal" role="form" name="contactForm" ng-submit="postContact(contactForm.$valid)">
          <div class="form-group" ng-class="{ 'has-error' : contactForm.name.$invalid && !contactForm.name.$pristine }">
              <label for="name" class="col-sm-2 control-label">Name</label>
              <div class="col-sm-9">
                  <input type="text" class="form-control" id="name" name="name" ng-model="name" required ng-minlength="3">
                  <p ng-show="contactForm.name.$error.minlength" class="help-block">The entered name is too short.</p>
              </div>
          </div>
          <div class="form-group" ng-class="{ 'has-error' : contactForm.phone.$invalid && !contactForm.phone.$pristine }">
              <label for="phone"  class="col-sm-2 control-label">Phone</label>
              <div class="col-sm-9">
                  <input type="text" class="form-control" id="phone" name="phone" ng-model="phone" required ng-minlength="7">
                  <p ng-show="contactForm.phone.$error.minlength" class="help-block">The entered phone number is too short.</p>
              </div>
          </div>
          <div class="form-group" ng-class="{ 'has-error' : contactForm.address.$invalid && !contactForm.address.$pristine }">
              <label for="address" class="col-sm-2 control-label">Address</label>
              <div class="col-sm-9">
                  <input type="text" class="form-control" id="address" name="address" ng-model="address" required >
                  <p ng-show="contactForm.address.$invalid && !contactForm.address.$pristine" class="help-block">The address is required.</p>
              </div>
          </div>
          <div class="form-group">
              <div class="col-sm-offset-2 col-sm-9">
                  <button type="submit" class="btn btn-default btn-primary" ng-disabled="contactForm.$invalid">Submit</button>
                  <a class="btn" href="contacts">Cancel</a>
              </div>
          </div>
      </form>
    </div>
</@base.page>
