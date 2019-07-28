function updateProgressBar(percentage, text, progressBarElem, progressBarDescriptionElem) {
  if(progressBarElem.innerHTML != percentage) {
      progressBarElem.style.width = percentage + '%';
      progressBarElem.innerHTML = percentage + '%';
      progressBarDescriptionElem.innerHTML = text;
  }
}