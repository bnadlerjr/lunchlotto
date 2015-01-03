window.lunchlotto = {};

lunchlotto.locationPicker = (function($) {
    var autocomplete, $latitudeInput, $longitudeInput;

    function updateCoordinates() {
        var location = autocomplete.getPlace().geometry.location;
        $latitudeInput.val(location.lat());
        $longitudeInput.val(location.lng());
    }

    return {
        init: function(locationInput, latitudeInput, longitudeInput) {
            var $el = $(locationInput)[0];
            if ($el) {
                $latitudeInput = $(latitudeInput);
                $longitudeInput = $(longitudeInput);
                autocomplete = new google.maps.places.Autocomplete($el, { types: ['geocode'] });
                google.maps.event.addListener(autocomplete, "place_changed", function() {
                    updateCoordinates();
                });
            }
        }
    }
}(jQuery));

jQuery(document).ready(function(){
    lunchlotto.locationPicker.init("#location", "#latitude", "#longitude");
})
