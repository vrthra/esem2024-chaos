require(data.table)
require(dplyr)
require(argparse)
require(ggplot2)

make_chart <- function(data, file_name, width, height) {
    chart <- data %>%
      mutate(estimator = factor(estimator, levels=c(
                                                    "Chao1",
                                                    "Chao2",
                                                    "Chao1-bc",
                                                    "Chao2-bc",
                                                    "iChao1",
                                                    "iChao2",
                                                    "Jack1",
                                                    "Jack2",
                                                    "ACE",
                                                    "ICE",
                                                    "ACE-1",
                                                    "ICE-1"
                                                    ))) %>%
    ggplot(aes(x = estimator, y = estimate)) +
      geom_point(size=.5, aes(color=kind)) +
      geom_errorbar(aes(ymin = CILower, ymax = CIUpper, color=kind), size = 0.4, width = .2) +
      scale_color_manual("kind", values=c("red", "blue")) +
      geom_hline(aes(yintercept = total_mutants), linetype = 1, color = 'black') +
      geom_hline(aes(yintercept = Sn), linetype = 2) +
      geom_hline(aes(yintercept = MEstimate), linetype = 3, color='black') +
      geom_hline(aes(yintercept = MUpper), linetype = 3, color='gray') +
      geom_hline(aes(yintercept = MLower), linetype = 3, color='gray') +
      coord_flip() +
      expand_limits(y = 0) +
      facet_wrap(~program, strip.position='top') +
      theme_light() +
      theme(strip.background = element_rect(fill="transparent"),
            strip.text = element_text(colour="black")
            ) +
      theme(
            #axis.text.x = element_text(colour = "black"),
            #axis.text.y = element_text(colour = "black"),
            text = element_text(size=8)) +
      theme(legend.position="none") +
      theme(
        axis.line=element_blank(),
        #axis.text.x=element_blank(),
        #axis.text.y=element_blank(),
        #axis.ticks=element_blank(),
        axis.title.x=element_blank(),
        axis.title.y=element_blank())
  print(paste("Saving", file_name))
  #pdf(file_name)
  ggsave(file_name, chart, height=height, width=width)

  #dev.off()
}

draw_charts <- function(input_path, suite, tmodel, output_dir, width, height) {
  print(paste0(input_path, tmodel))
  sdata = fread(input_path)
  sdata = sdata[testsuite == suite]
  sdata = sdata[model == tmodel]
  sdata = sdata[estimator != "Manual"]
  subjects = unique(sdata[, .(program)])[[1]]
  dir.create(output_dir)
  for (subject_ in subjects) {
    chart_file = paste0(output_dir, '/', subject_, '.pdf')
    subj_data = sdata[program == subject_]
    print(paste("Plotting", subject_))
    make_chart(subj_data, chart_file, width, height)
  }
}

if (sys.nframe() == 0 || TRUE) {
  parser = ArgumentParser()
  parser$add_argument("-s", "--summary", help = "summary table")
  parser$add_argument("-t", "--testsuite", help = "testsuite to draw")
  parser$add_argument("-o", "--output", help = "Filename for the resulting comparison table")
  parser$add_argument("-M", "--model", help = "model -- abundance or incidence")
  parser$add_argument("-H", "--height", type="double", default=1.5, help = "height of the graph")
  parser$add_argument("-W", "--width", type="double", default=3.0, help = "width of the graph")
  args = parser$parse_args()
  my_data <- args$summary
  draw_charts(my_data, args$testsuite, args$model, args$output, args$width, args$height)
}
